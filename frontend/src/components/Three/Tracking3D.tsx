import React, { useRef, useState, useEffect } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { useAppSelector } from '../../hooks/redux';
import { Box, Typography, CircularProgress, Paper, Button } from '@mui/material';

interface Tracking3DProps {
  deliveryId?: string;
  height?: string;
}

const Tracking3D: React.FC<Tracking3DProps> = ({ deliveryId, height = '500px' }) => {
  const mountRef = useRef<HTMLDivElement>(null);
  const sceneRef = useRef<THREE.Scene | null>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  const frameRef = useRef<number>(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { trackingData } = useAppSelector(state => state.tracking);

  useEffect(() => {
    if (!mountRef.current) return;

    // Initialiser la scène Three.js
    const width = mountRef.current.clientWidth;
    const height = mountRef.current.clientHeight;

    // Scene
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(0xf0f0f0);
    sceneRef.current = scene;

    // Camera
    const camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000);
    camera.position.set(0, 10, 20);

    // Renderer
    const renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(width, height);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    mountRef.current.appendChild(renderer.domElement);
    rendererRef.current = renderer;

    // Controls
    const controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;

    // Lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
    directionalLight.position.set(10, 20, 10);
    directionalLight.castShadow = true;
    directionalLight.shadow.camera.left = -50;
    directionalLight.shadow.camera.right = 50;
    directionalLight.shadow.camera.top = 50;
    directionalLight.shadow.camera.bottom = -50;
    scene.add(directionalLight);

    // Grid Helper
    const gridHelper = new THREE.GridHelper(100, 50, 0x888888, 0xcccccc);
    scene.add(gridHelper);

    // Ground
    const groundGeometry = new THREE.PlaneGeometry(100, 100);
    const groundMaterial = new THREE.MeshStandardMaterial({ 
      color: 0xeeeeee,
      roughness: 0.8,
      metalness: 0.2
    });
    const ground = new THREE.Mesh(groundGeometry, groundMaterial);
    ground.rotation.x = -Math.PI / 2;
    ground.receiveShadow = true;
    scene.add(ground);

    // Animation loop
    const animate = () => {
      frameRef.current = requestAnimationFrame(animate);
      controls.update();
      renderer.render(scene, camera);
    };
    animate();

    // Handle resize
    const handleResize = () => {
      if (!mountRef.current) return;
      const width = mountRef.current.clientWidth;
      const height = mountRef.current.clientHeight;
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      renderer.setSize(width, height);
    };
    window.addEventListener('resize', handleResize);

    // Cleanup
    return () => {
      window.removeEventListener('resize', handleResize);
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current);
      }
      if (mountRef.current && renderer.domElement) {
        mountRef.current.removeChild(renderer.domElement);
      }
      renderer.dispose();
    };
  }, []);

  // Mettre à jour la visualisation lorsque les données de suivi changent
  useEffect(() => {
    if (!deliveryId || !trackingData[deliveryId] || !sceneRef.current) return;

    setIsLoading(false);
    const scene = sceneRef.current;

    // Supprimer les anciens objets de suivi
    const oldTrackingObjects = scene.children.filter(child => child.userData.isTrackingObject);
    oldTrackingObjects.forEach(obj => scene.remove(obj));

    const trackingInfo = trackingData[deliveryId];
    const points = trackingInfo.points;

    if (points.length < 2) return;

    // Créer une courbe à partir des points de suivi
    const curvePoints = points.map(point => {
      // Conversion des coordonnées GPS en coordonnées 3D (simplifiée)
      const x = (point.longitude - points[0].longitude) * 100; // Conversion simplifiée
      const z = (point.latitude - points[0].latitude) * 100; // Conversion simplifiée
      return new THREE.Vector3(x, 0.5, z);
    });

    const curve = new THREE.CatmullRomCurve3(curvePoints);

    // Visualiser la trajectoire
    const tubeGeometry = new THREE.TubeGeometry(curve, 100, 0.2, 8, false);
    const tubeMaterial = new THREE.MeshStandardMaterial({ 
      color: 0x1976d2,
      roughness: 0.5,
      metalness: 0.5
    });
    const tube = new THREE.Mesh(tubeGeometry, tubeMaterial);
    tube.castShadow = true;
    tube.userData.isTrackingObject = true;
    scene.add(tube);

    // Ajouter des sphères pour chaque point de suivi
    points.forEach((point, index) => {
      const x = (point.longitude - points[0].longitude) * 100;
      const z = (point.latitude - points[0].latitude) * 100;

      const sphereGeometry = new THREE.SphereGeometry(0.3, 16, 16);
      const sphereMaterial = new THREE.MeshStandardMaterial({ 
        color: index === 0 ? 0x4caf50 : (index === points.length - 1 ? 0xf44336 : 0x2196f3)
      });
      const sphere = new THREE.Mesh(sphereGeometry, sphereMaterial);
      sphere.position.set(x, 0.5, z);
      sphere.castShadow = true;
      sphere.userData.isTrackingObject = true;
      scene.add(sphere);
    });

    // Ajouter une icône de camion pour la position actuelle
    if (trackingInfo.isActive && points.length > 0) {
      const lastPoint = points[points.length - 1];
      const x = (lastPoint.longitude - points[0].longitude) * 100;
      const z = (lastPoint.latitude - points[0].latitude) * 100;

      // Groupe pour représenter le camion
      const truck = new THREE.Group();
      truck.userData.isTrackingObject = true;

      // Corps du camion
      const bodyGeometry = new THREE.BoxGeometry(1.5, 0.8, 0.8);
      const bodyMaterial = new THREE.MeshStandardMaterial({ color: 0xff9800 });
      const body = new THREE.Mesh(bodyGeometry, bodyMaterial);
      body.position.y = 0.4;
      body.castShadow = true;
      truck.add(body);

      // Cabine du camion
      const cabinGeometry = new THREE.BoxGeometry(0.8, 0.6, 0.8);
      const cabinMaterial = new THREE.MeshStandardMaterial({ color: 0xf57c00 });
      const cabin = new THREE.Mesh(cabinGeometry, cabinMaterial);
      cabin.position.set(0.7, 0.3, 0);
      cabin.castShadow = true;
      truck.add(cabin);

      // Roues
      const wheelGeometry = new THREE.CylinderGeometry(0.2, 0.2, 0.2, 16);
      const wheelMaterial = new THREE.MeshStandardMaterial({ color: 0x212121 });

      const positions = [
        { x: 0.5, z: 0.4 },
        { x: 0.5, z: -0.4 },
        { x: -0.5, z: 0.4 },
        { x: -0.5, z: -0.4 }
      ];

      positions.forEach(pos => {
        const wheel = new THREE.Mesh(wheelGeometry, wheelMaterial);
        wheel.position.set(pos.x, 0.2, pos.z);
        wheel.rotation.z = Math.PI / 2;
        wheel.castShadow = true;
        truck.add(wheel);
      });

      // Orienter le camion dans la direction du mouvement
      if (points.length > 1) {
        const prevPoint = points[points.length - 2];
        const dx = (lastPoint.longitude - prevPoint.longitude) * 100;
        const dz = (lastPoint.latitude - prevPoint.latitude) * 100;
        const angle = Math.atan2(dx, dz);
        truck.rotation.y = angle;
      }

      truck.position.set(x, 0, z);
      scene.add(truck);
    }
  }, [deliveryId, trackingData]);

  const handleRefresh = () => {
    setIsLoading(true);
    // Ici vous pourriez appeler une action pour rafraîchir les données de suivi
    setTimeout(() => {
      setIsLoading(false);
    }, 1000);
  };

  return (
    <Paper sx={{ p: 2, height, display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Suivi 3D de la livraison</Typography>
        <Button variant="outlined" onClick={handleRefresh} disabled={isLoading}>
          {isLoading ? <CircularProgress size={20} /> : 'Actualiser'}
        </Button>
      </Box>

      {error && (
        <Box sx={{ mb: 2, p: 2, bgcolor: 'error.main', color: 'white', borderRadius: 1 }}>
          <Typography>{error}</Typography>
        </Box>
      )}

      <Box sx={{ flexGrow: 1, position: 'relative' }}>
        {isLoading && (
          <Box
            sx={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              bgcolor: 'rgba(255, 255, 255, 0.7)',
              zIndex: 1
            }}
          >
            <CircularProgress />
          </Box>
        )}
        <div ref={mountRef} style={{ width: '100%', height: '100%' }} />
      </Box>
    </Paper>
  );
};

export default Tracking3D;
