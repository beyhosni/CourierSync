import React, { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader';

interface LocationUpdate {
  id: string;
  driverId: string;
  deliveryId?: string;
  latitude: number;
  longitude: number;
  timestamp: string;
}

interface DeliveryVisualization3DProps {
  locationUpdates: LocationUpdate[];
  selectedDriverId?: string;
  selectedDeliveryId?: string;
}

const DeliveryVisualization3D: React.FC<DeliveryVisualization3DProps> = ({
  locationUpdates,
  selectedDriverId,
  selectedDeliveryId
}) => {
  const mountRef = useRef<HTMLDivElement>(null);
  const sceneRef = useRef<THREE.Scene>();
  const rendererRef = useRef<THREE.WebGLRenderer>();
  const cameraRef = useRef<THREE.PerspectiveCamera>();
  const controlsRef = useRef<OrbitControls>();
  const animationFrameRef = useRef<number>();
  const driverMeshesRef = useRef<Map<string, THREE.Mesh>>(new Map());
  const deliveryPathsRef = useRef<Map<string, THREE.Line>>(new Map());
  const [isLoaded, setIsLoaded] = useState(false);

  // Convert latitude/longitude to 3D coordinates
  const latLngToVector3 = (lat: number, lng: number, height: number = 0): THREE.Vector3 => {
    const radius = 100; // Earth radius in our 3D world
    const phi = (90 - lat) * (Math.PI / 180);
    const theta = (lng + 180) * (Math.PI / 180);

    const x = -(radius * Math.sin(phi) * Math.cos(theta));
    const z = radius * Math.sin(phi) * Math.sin(theta);
    const y = radius * Math.cos(phi);

    return new THREE.Vector3(x, y + height, z);
  };

  // Create driver marker
  const createDriverMarker = (color: number = 0xff0000): THREE.Mesh => {
    const geometry = new THREE.ConeGeometry(1, 2, 8);
    const material = new THREE.MeshStandardMaterial({ 
      color,
      emissive: color,
      emissiveIntensity: 0.2
    });
    const mesh = new THREE.Mesh(geometry, material);
    mesh.castShadow = true;
    return mesh;
  };

  // Create delivery path
  const createDeliveryPath = (points: THREE.Vector3[], color: number = 0x0088ff): THREE.Line => {
    const geometry = new THREE.BufferGeometry().setFromPoints(points);
    const material = new THREE.LineBasicMaterial({ 
      color, 
      linewidth: 2,
      opacity: 0.7,
      transparent: true
    });
    return new THREE.Line(geometry, material);
  };

  // Initialize Three.js scene
  useEffect(() => {
    if (!mountRef.current) return;

    // Scene setup
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(0x000033);
    scene.fog = new THREE.Fog(0x000033, 10, 500);
    sceneRef.current = scene;

    // Camera setup
    const camera = new THREE.PerspectiveCamera(
      75,
      mountRef.current.clientWidth / mountRef.current.clientHeight,
      0.1,
      1000
    );
    camera.position.set(0, 50, 150);
    cameraRef.current = camera;

    // Renderer setup
    const renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(mountRef.current.clientWidth, mountRef.current.clientHeight);
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    mountRef.current.appendChild(renderer.domElement);
    rendererRef.current = renderer;

    // Controls setup
    const controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    controls.minDistance = 20;
    controls.maxDistance = 500;
    controlsRef.current = controls;

    // Lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.4);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
    directionalLight.position.set(50, 100, 50);
    directionalLight.castShadow = true;
    directionalLight.shadow.camera.near = 0.1;
    directionalLight.shadow.camera.far = 500;
    directionalLight.shadow.camera.left = -100;
    directionalLight.shadow.camera.right = 100;
    directionalLight.shadow.camera.top = 100;
    directionalLight.shadow.camera.bottom = -100;
    scene.add(directionalLight);

    // Create Earth sphere
    const earthGeometry = new THREE.SphereGeometry(100, 32, 32);
    const earthMaterial = new THREE.MeshStandardMaterial({
      color: 0x2233ff,
      emissive: 0x112244,
      roughness: 0.7,
      metalness: 0.3
    });
    const earth = new THREE.Mesh(earthGeometry, earthMaterial);
    earth.receiveShadow = true;
    scene.add(earth);

    // Add grid helper
    const gridHelper = new THREE.GridHelper(300, 30, 0x444444, 0x222222);
    gridHelper.position.y = -100;
    scene.add(gridHelper);

    setIsLoaded(true);

    // Animation loop
    const animate = () => {
      animationFrameRef.current = requestAnimationFrame(animate);

      // Rotate earth slowly
      earth.rotation.y += 0.0001;

      // Update controls
      controls.update();

      // Render scene
      renderer.render(scene, camera);
    };
    animate();

    // Handle window resize
    const handleResize = () => {
      if (!mountRef.current) return;
      camera.aspect = mountRef.current.clientWidth / mountRef.current.clientHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(mountRef.current.clientWidth, mountRef.current.clientHeight);
    };
    window.addEventListener('resize', handleResize);

    // Cleanup
    return () => {
      window.removeEventListener('resize', handleResize);
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
      if (mountRef.current && renderer.domElement) {
        mountRef.current.removeChild(renderer.domElement);
      }
      renderer.dispose();
    };
  }, []);

  // Update visualization when location updates change
  useEffect(() => {
    if (!sceneRef.current || !isLoaded) return;

    // Remove existing meshes and paths
    driverMeshesRef.current.forEach(mesh => {
      sceneRef.current?.remove(mesh);
    });
    deliveryPathsRef.current.forEach(path => {
      sceneRef.current?.remove(path);
    });

    driverMeshesRef.current.clear();
    deliveryPathsRef.current.clear();

    // Group location updates by driver
    const driverUpdates = new Map<string, LocationUpdate[]>();

    locationUpdates.forEach(update => {
      if (!driverUpdates.has(update.driverId)) {
        driverUpdates.set(update.driverId, []);
      }
      driverUpdates.get(update.driverId)?.push(update);
    });

    // Create visualizations for each driver
    driverUpdates.forEach((updates, driverId) => {
      // Sort updates by timestamp
      updates.sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

      // Check if this driver is selected
      const isSelected = driverId === selectedDriverId;
      const driverColor = isSelected ? 0xffaa00 : 0xff0000;

      // Create driver marker at latest position
      const latestUpdate = updates[updates.length - 1];
      if (latestUpdate) {
        const driverPosition = latLngToVector3(latestUpdate.latitude, latestUpdate.longitude, 2);
        const driverMarker = createDriverMarker(driverColor);
        driverMarker.position.copy(driverPosition);
        driverMeshesRef.current.set(driverId, driverMarker);
        sceneRef.current.add(driverMarker);
      }

      // Create path for driver
      if (updates.length > 1) {
        const pathPoints = updates.map(update => 
          latLngToVector3(update.latitude, update.longitude, 1)
        );
        const pathColor = isSelected ? 0xffaa00 : 0x0088ff;
        const path = createDeliveryPath(pathPoints, pathColor);
        deliveryPathsRef.current.set(driverId, path);
        sceneRef.current.add(path);
      }
    });
  }, [locationUpdates, selectedDriverId, isLoaded]);

  return <div ref={mountRef} className="w-full h-full" />;
};

export default DeliveryVisualization3D;
