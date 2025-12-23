import React, { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { Invoice } from '../../types/billing';

interface BillingReports3DProps {
  invoices: Invoice[];
  reportType?: 'revenue' | 'customers' | 'services';
  timeRange?: 'week' | 'month' | 'quarter' | 'year';
}

const BillingReports3D: React.FC<BillingReports3DProps> = ({
  invoices,
  reportType = 'revenue',
  timeRange = 'month'
}) => {
  const mountRef = useRef<HTMLDivElement>(null);
  const sceneRef = useRef<THREE.Scene>();
  const rendererRef = useRef<THREE.WebGLRenderer>();
  const cameraRef = useRef<THREE.PerspectiveCamera>();
  const controlsRef = useRef<OrbitControls>();
  const animationFrameRef = useRef<number>();
  const [isLoaded, setIsLoaded] = useState(false);

  // Initialize Three.js scene
  useEffect(() => {
    if (!mountRef.current) return;

    // Scene setup
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(0x0a0a0a);
    scene.fog = new THREE.Fog(0x0a0a0a, 10, 500);
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

    setIsLoaded(true);

    // Animation loop
    const animate = () => {
      animationFrameRef.current = requestAnimationFrame(animate);
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

  // Update visualization when invoices or report type change
  useEffect(() => {
    if (!sceneRef.current || !isLoaded) return;

    // Remove existing visualization
    const existingVisualization = sceneRef.current.children.filter(child => child.userData.isBillingReport);
    existingVisualization.forEach(child => sceneRef.current?.remove(child));

    if (reportType === 'revenue') {
      // Create revenue visualization
      // Group invoices by time period based on timeRange
      const revenueByPeriod = new Map<string, number>();

      invoices.forEach(invoice => {
        if (invoice.status !== 'paid') return; // Only count paid invoices

        const date = new Date(invoice.issueDate);
        let periodKey = '';

        switch (timeRange) {
          case 'week':
            const weekStart = new Date(date);
            weekStart.setDate(date.getDate() - date.getDay());
            periodKey = weekStart.toISOString().split('T')[0];
            break;
          case 'month':
            periodKey = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`;
            break;
          case 'quarter':
            const quarter = Math.floor(date.getMonth() / 3) + 1;
            periodKey = `${date.getFullYear()}-Q${quarter}`;
            break;
          case 'year':
            periodKey = date.getFullYear().toString();
            break;
        }

        const currentRevenue = revenueByPeriod.get(periodKey) || 0;
        revenueByPeriod.set(periodKey, currentRevenue + invoice.totalAmount);
      });

      // Create 3D bar chart
      const sortedPeriods = Array.from(revenueByPeriod.keys()).sort();
      const maxRevenue = Math.max(...Array.from(revenueByPeriod.values()));

      sortedPeriods.forEach((period, index) => {
        const revenue = revenueByPeriod.get(period) || 0;
        const height = Math.min(50, (revenue / maxRevenue) * 50);

        const geometry = new THREE.BoxGeometry(6, height, 6);
        const material = new THREE.MeshPhongMaterial({
          color: 0x00ff88,
          emissive: 0x00ff88,
          emissiveIntensity: 0.2
        });

        const mesh = new THREE.Mesh(geometry, material);
        mesh.position.x = (index - sortedPeriods.length / 2) * 10;
        mesh.position.y = height / 2;
        mesh.castShadow = true;
        mesh.receiveShadow = true;
        mesh.userData.isBillingReport = true;

        sceneRef.current.add(mesh);

        // Add a label for the period
        const canvas = document.createElement('canvas');
        canvas.width = 256;
        canvas.height = 64;
        const context = canvas.getContext('2d')!;
        context.fillStyle = '#ffffff';
        context.font = '20px Arial';
        context.fillText(period, 10, 30);
        context.fillText(`€${revenue.toFixed(2)}`, 10, 55);

        const texture = new THREE.CanvasTexture(canvas);
        const spriteMaterial = new THREE.SpriteMaterial({ map: texture });
        const sprite = new THREE.Sprite(spriteMaterial);
        sprite.scale.set(8, 2, 1);
        sprite.position.copy(mesh.position);
        sprite.position.y += height / 2 + 2;
        sprite.userData.isBillingReport = true;

        sceneRef.current.add(sprite);
      });
    } else if (reportType === 'customers') {
      // Create customer visualization
      const revenueByCustomer = new Map<string, number>();

      invoices.forEach(invoice => {
        if (invoice.status !== 'paid') return; // Only count paid invoices

        const currentRevenue = revenueByCustomer.get(invoice.customerName) || 0;
        revenueByCustomer.set(invoice.customerName, currentRevenue + invoice.totalAmount);
      });

      // Sort customers by revenue and take top 10
      const sortedCustomers = Array.from(revenueByCustomer.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10);

      const maxRevenue = Math.max(...sortedCustomers.map(([, revenue]) => revenue));

      sortedCustomers.forEach(([customerName, revenue], index) => {
        const height = Math.min(50, (revenue / maxRevenue) * 50);

        const geometry = new THREE.CylinderGeometry(3, 3, height, 32);
        const material = new THREE.MeshPhongMaterial({
          color: 0x00aaff,
          emissive: 0x00aaff,
          emissiveIntensity: 0.2
        });

        const mesh = new THREE.Mesh(geometry, material);
        mesh.position.x = (index - sortedCustomers.length / 2) * 10;
        mesh.position.y = height / 2;
        mesh.castShadow = true;
        mesh.receiveShadow = true;
        mesh.userData.isBillingReport = true;

        sceneRef.current.add(mesh);

        // Add a label for the customer
        const canvas = document.createElement('canvas');
        canvas.width = 256;
        canvas.height = 64;
        const context = canvas.getContext('2d')!;
        context.fillStyle = '#ffffff';
        context.font = '18px Arial';
        const displayName = customerName.length > 15 ? customerName.substring(0, 15) + '...' : customerName;
        context.fillText(displayName, 10, 30);
        context.fillText(`€${revenue.toFixed(2)}`, 10, 55);

        const texture = new THREE.CanvasTexture(canvas);
        const spriteMaterial = new THREE.SpriteMaterial({ map: texture });
        const sprite = new THREE.Sprite(spriteMaterial);
        sprite.scale.set(8, 2, 1);
        sprite.position.copy(mesh.position);
        sprite.position.y += height / 2 + 2;
        sprite.userData.isBillingReport = true;

        sceneRef.current.add(sprite);
      });
    } else if (reportType === 'services') {
      // Create services visualization
      const revenueByService = new Map<string, number>();

      invoices.forEach(invoice => {
        if (invoice.status !== 'paid') return; // Only count paid invoices

        invoice.items.forEach(item => {
          const currentRevenue = revenueByService.get(item.description) || 0;
          revenueByService.set(item.description, currentRevenue + item.lineTotal);
        });
      });

      // Sort services by revenue and take top 10
      const sortedServices = Array.from(revenueByService.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10);

      const maxRevenue = Math.max(...sortedServices.map(([, revenue]) => revenue));

      sortedServices.forEach(([serviceName, revenue], index) => {
        const height = Math.min(50, (revenue / maxRevenue) * 50);

        const geometry = new THREE.OctahedronGeometry(3);
        const material = new THREE.MeshPhongMaterial({
          color: 0xff8800,
          emissive: 0xff8800,
          emissiveIntensity: 0.2
        });

        const mesh = new THREE.Mesh(geometry, material);
        mesh.position.x = (index - sortedServices.length / 2) * 10;
        mesh.position.y = height / 2;
        mesh.castShadow = true;
        mesh.receiveShadow = true;
        mesh.userData.isBillingReport = true;

        sceneRef.current.add(mesh);

        // Add a label for the service
        const canvas = document.createElement('canvas');
        canvas.width = 256;
        canvas.height = 64;
        const context = canvas.getContext('2d')!;
        context.fillStyle = '#ffffff';
        context.font = '18px Arial';
        const displayName = serviceName.length > 20 ? serviceName.substring(0, 20) + '...' : serviceName;
        context.fillText(displayName, 10, 30);
        context.fillText(`€${revenue.toFixed(2)}`, 10, 55);

        const texture = new THREE.CanvasTexture(canvas);
        const spriteMaterial = new THREE.SpriteMaterial({ map: texture });
        const sprite = new THREE.Sprite(spriteMaterial);
        sprite.scale.set(8, 2, 1);
        sprite.position.copy(mesh.position);
        sprite.position.y += height / 2 + 2;
        sprite.userData.isBillingReport = true;

        sceneRef.current.add(sprite);
      });
    }
  }, [invoices, reportType, timeRange, isLoaded]);

  return <div ref={mountRef} className="w-full h-full" />;
};

export default BillingReports3D;
