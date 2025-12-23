import React, { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader';
import { Invoice } from '../../types/billing';

interface BillingVisualization3DProps {
  invoices: Invoice[];
  selectedInvoiceId?: string;
  viewMode?: 'overview' | 'comparison' | 'trends';
}

const BillingVisualization3D: React.FC<BillingVisualization3DProps> = ({
  invoices,
  selectedInvoiceId,
  viewMode = 'overview'
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

  // Update visualization when invoices change
  useEffect(() => {
    if (!sceneRef.current || !isLoaded) return;

    // Remove existing visualization
    const existingVisualization = sceneRef.current.children.filter(child => child.userData.isBillingVisualization);
    existingVisualization.forEach(child => sceneRef.current?.remove(child));

    if (viewMode === 'overview') {
      // Create 3D overview of invoices
      invoices.forEach((invoice, index) => {
        // Create a bar for each invoice
        const height = Math.min(50, Math.max(2, invoice.totalAmount / 100));
        const geometry = new THREE.BoxGeometry(4, height, 4);

        // Color based on status
        let color = 0x0088ff; // Default blue
        switch (invoice.status) {
          case 'draft': color = 0x888888; break;
          case 'sent': color = 0xffaa00; break;
          case 'paid': color = 0x00ff00; break;
          case 'overdue': color = 0xff0000; break;
          case 'cancelled': color = 0x880000; break;
        }

        const material = new THREE.MeshPhongMaterial({
          color,
          emissive: color,
          emissiveIntensity: invoice.id === selectedInvoiceId ? 0.3 : 0.1
        });

        const mesh = new THREE.Mesh(geometry, material);
        mesh.position.x = (index % 10) * 8 - 40;
        mesh.position.z = Math.floor(index / 10) * 8 - 40;
        mesh.position.y = height / 2;
        mesh.castShadow = true;
        mesh.receiveShadow = true;
        mesh.userData.isBillingVisualization = true;
        mesh.userData.invoiceId = invoice.id;

        sceneRef.current.add(mesh);

        // Add a label for the invoice number
        const canvas = document.createElement('canvas');
        canvas.width = 256;
        canvas.height = 64;
        const context = canvas.getContext('2d')!;
        context.fillStyle = '#ffffff';
        context.font = '24px Arial';
        context.fillText(invoice.invoiceNumber, 10, 40);

        const texture = new THREE.CanvasTexture(canvas);
        const spriteMaterial = new THREE.SpriteMaterial({ map: texture });
        const sprite = new THREE.Sprite(spriteMaterial);
        sprite.scale.set(8, 2, 1);
        sprite.position.copy(mesh.position);
        sprite.position.y += height / 2 + 2;
        sprite.userData.isBillingVisualization = true;

        sceneRef.current.add(sprite);
      });
    } else if (viewMode === 'comparison') {
      // Create a comparison chart of invoices by status
      const statusCounts = {
        draft: invoices.filter(i => i.status === 'draft').length,
        sent: invoices.filter(i => i.status === 'sent').length,
        paid: invoices.filter(i => i.status === 'paid').length,
        overdue: invoices.filter(i => i.status === 'overdue').length,
        cancelled: invoices.filter(i => i.status === 'cancelled').length,
      };

      let x = -40;
      Object.entries(statusCounts).forEach(([status, count]) => {
        if (count === 0) return;

        const height = Math.min(50, count * 5);
        const geometry = new THREE.BoxGeometry(8, height, 8);

        // Color based on status
        let color = 0x0088ff; // Default blue
        switch (status) {
          case 'draft': color = 0x888888; break;
          case 'sent': color = 0xffaa00; break;
          case 'paid': color = 0x00ff00; break;
          case 'overdue': color = 0xff0000; break;
          case 'cancelled': color = 0x880000; break;
        }

        const material = new THREE.MeshPhongMaterial({
          color,
          emissive: color,
          emissiveIntensity: 0.2
        });

        const mesh = new THREE.Mesh(geometry, material);
        mesh.position.x = x;
        mesh.position.y = height / 2;
        mesh.castShadow = true;
        mesh.receiveShadow = true;
        mesh.userData.isBillingVisualization = true;

        sceneRef.current.add(mesh);

        // Add a label for the status
        const canvas = document.createElement('canvas');
        canvas.width = 256;
        canvas.height = 64;
        const context = canvas.getContext('2d')!;
        context.fillStyle = '#ffffff';
        context.font = '24px Arial';
        context.fillText(`${status}: ${count}`, 10, 40);

        const texture = new THREE.CanvasTexture(canvas);
        const spriteMaterial = new THREE.SpriteMaterial({ map: texture });
        const sprite = new THREE.Sprite(spriteMaterial);
        sprite.scale.set(10, 2.5, 1);
        sprite.position.copy(mesh.position);
        sprite.position.y += height / 2 + 2;
        sprite.userData.isBillingVisualization = true;

        sceneRef.current.add(sprite);

        x += 20;
      });
    } else if (viewMode === 'trends') {
      // Create a line chart showing invoice totals over time
      const sortedInvoices = [...invoices].sort((a, b) => 
        new Date(a.issueDate).getTime() - new Date(b.issueDate).getTime()
      );

      if (sortedInvoices.length > 0) {
        const points: THREE.Vector3[] = [];
        const maxAmount = Math.max(...sortedInvoices.map(i => i.totalAmount));

        sortedInvoices.forEach((invoice, index) => {
          const x = (index / (sortedInvoices.length - 1)) * 80 - 40;
          const y = (invoice.totalAmount / maxAmount) * 40;
          const z = 0;

          points.push(new THREE.Vector3(x, y, z));

          // Add a point marker
          const geometry = new THREE.SphereGeometry(1, 16, 16);
          const material = new THREE.MeshPhongMaterial({
            color: invoice.id === selectedInvoiceId ? 0xffaa00 : 0x0088ff,
            emissive: invoice.id === selectedInvoiceId ? 0xffaa00 : 0x0088ff,
            emissiveIntensity: 0.3
          });

          const mesh = new THREE.Mesh(geometry, material);
          mesh.position.copy(points[index]);
          mesh.castShadow = true;
          mesh.receiveShadow = true;
          mesh.userData.isBillingVisualization = true;
          mesh.userData.invoiceId = invoice.id;

          sceneRef.current.add(mesh);
        });

        // Create a line through the points
        const geometry = new THREE.BufferGeometry().setFromPoints(points);
        const material = new THREE.LineBasicMaterial({ 
          color: 0x0088ff,
          linewidth: 2
        });

        const line = new THREE.Line(geometry, material);
        line.userData.isBillingVisualization = true;
        sceneRef.current.add(line);
      }
    }
  }, [invoices, selectedInvoiceId, viewMode, isLoaded]);

  return <div ref={mountRef} className="w-full h-full" />;
};

export default BillingVisualization3D;
