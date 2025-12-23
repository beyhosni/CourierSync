import React, { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { DeliveryPoint, DeliveryRoute } from '../../types/delivery';

interface DeliveryMap3DProps {
  deliveryPoints: DeliveryPoint[];
  deliveryRoutes: DeliveryRoute[];
  currentDriverPosition?: { lat: number; lng: number };
  viewMode?: 'driver' | 'dispatcher' | 'customer';
  onPointClick?: (point: DeliveryPoint) => void;
}

const DeliveryMap3D: React.FC<DeliveryMap3DProps> = ({
  deliveryPoints,
  deliveryRoutes,
  currentDriverPosition,
  viewMode = 'dispatcher',
  onPointClick
}) => {
  const mountRef = useRef<HTMLDivElement>(null);
  const sceneRef = useRef<THREE.Scene>();
  const rendererRef = useRef<THREE.WebGLRenderer>();
  const cameraRef = useRef<THREE.PerspectiveCamera>();
  const controlsRef = useRef<OrbitControls>();
  const frameRef = useRef<number>();
  const [isLoaded, setIsLoaded] = useState(false);

  // Convert lat/lng to 3D coordinates
  const latLngToVector3 = (lat: number, lng: number, height: number = 0): THREE.Vector3 => {
    const radius = 100; // Earth radius in our 3D world
    const phi = (90 - lat) * (Math.PI / 180);
    const theta = (lng + 180) * (Math.PI / 180);

    const x = -(radius * Math.sin(phi) * Math.cos(theta));
    const z = radius * Math.sin(phi) * Math.sin(theta);
    const y = radius * Math.cos(phi);

    return new THREE.Vector3(x, y + height, z);
  };

  // Initialize Three.js scene
  useEffect(() => {
    if (!mountRef.current) return;

    // Scene setup
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(0x050505);
    scene.fog = new THREE.Fog(0x050505, 10, 500);
    sceneRef.current = scene;

    // Camera setup
    const camera = new THREE.PerspectiveCamera(
      75,
      mountRef.current.clientWidth / mountRef.current.clientHeight,
      0.1,
      1000
    );
    camera.position.set(0, 50, 100);
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
    controls.minDistance = 10;
    controls.maxDistance = 200;
    controlsRef.current = controls;

    // Lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.3);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.7);
    directionalLight.position.set(50, 100, 50);
    directionalLight.castShadow = true;
    directionalLight.shadow.camera.near = 0.1;
    directionalLight.shadow.camera.far = 500;
    directionalLight.shadow.camera.left = -100;
    directionalLight.shadow.camera.right = 100;
    directionalLight.shadow.camera.top = 100;
    directionalLight.shadow.camera.bottom = -100;
    scene.add(directionalLight);

    // Add a grid helper
    const gridHelper = new THREE.GridHelper(200, 20, 0x444444, 0x222222);
    gridHelper.position.y = -0.01;
    scene.add(gridHelper);

    // Animation loop
    const animate = () => {
      frameRef.current = requestAnimationFrame(animate);
      controls.update();
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

    setIsLoaded(true);

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

  // Update delivery points
  useEffect(() => {
    if (!sceneRef.current || !isLoaded) return;

    // Remove existing delivery points
    const existingPoints = sceneRef.current.children.filter(child => child.userData.isDeliveryPoint);
    existingPoints.forEach(point => sceneRef.current?.remove(point));

    // Add delivery points
    deliveryPoints.forEach((point, index) => {
      const position = latLngToVector3(point.lat, point.lng, point.type === 'pickup' ? 2 : 1);

      // Create point geometry
      const geometry = new THREE.SphereGeometry(1, 16, 16);
      const material = new THREE.MeshPhongMaterial({
        color: point.type === 'pickup' ? 0x00ff00 : 0xff0000,
        emissive: point.type === 'pickup' ? 0x00ff00 : 0xff0000,
        emissiveIntensity: 0.3
      });
      const mesh = new THREE.Mesh(geometry, material);
      mesh.position.copy(position);
      mesh.castShadow = true;
      mesh.receiveShadow = true;
      mesh.userData.isDeliveryPoint = true;
      mesh.userData.pointData = point;

      // Add pulsing animation
      const pulseScale = 1 + Math.sin(Date.now() * 0.002 + index) * 0.2;
      mesh.scale.set(pulseScale, pulseScale, pulseScale);

      sceneRef.current.add(mesh);

      // Add label
      const labelGeometry = new THREE.BoxGeometry(4, 1, 1);
      const labelMaterial = new THREE.MeshBasicMaterial({ color: 0xffffff });
      const label = new THREE.Mesh(labelGeometry, labelMaterial);
      label.position.copy(position);
      label.position.y += 2;
      label.userData.isDeliveryPoint = true;
      sceneRef.current.add(label);
    });
  }, [deliveryPoints, isLoaded]);

  // Update delivery routes
  useEffect(() => {
    if (!sceneRef.current || !isLoaded) return;

    // Remove existing routes
    const existingRoutes = sceneRef.current.children.filter(child => child.userData.isDeliveryRoute);
    existingRoutes.forEach(route => sceneRef.current?.remove(route));

    // Add delivery routes
    deliveryRoutes.forEach(route => {
      const points = route.points.map(point => latLngToVector3(point.lat, point.lng, 0.5));

      const geometry = new THREE.BufferGeometry().setFromPoints(points);
      const material = new THREE.LineBasicMaterial({
        color: 0x0088ff,
        linewidth: 2,
        opacity: 0.7,
        transparent: true
      });

      const line = new THREE.Line(geometry, material);
      line.userData.isDeliveryRoute = true;
      sceneRef.current.add(line);
    });
  }, [deliveryRoutes, isLoaded]);

  // Update driver position
  useEffect(() => {
    if (!sceneRef.current || !isLoaded || !currentDriverPosition) return;

    // Remove existing driver marker
    const existingDriver = sceneRef.current.children.find(child => child.userData.isDriver);
    if (existingDriver) {
      sceneRef.current.remove(existingDriver);
    }

    // Add driver marker
    const position = latLngToVector3(currentDriverPosition.lat, currentDriverPosition.lng, 3);

    const geometry = new THREE.ConeGeometry(1, 2, 8);
    const material = new THREE.MeshPhongMaterial({
      color: 0xffff00,
      emissive: 0xffff00,
      emissiveIntensity: 0.3
    });
    const driverMesh = new THREE.Mesh(geometry, material);
    driverMesh.position.copy(position);
    driverMesh.castShadow = true;
    driverMesh.receiveShadow = true;
    driverMesh.userData.isDriver = true;

    sceneRef.current.add(driverMesh);
  }, [currentDriverPosition, isLoaded]);

  return <div ref={mountRef} className="w-full h-full" />;
};

export default DeliveryMap3D;
