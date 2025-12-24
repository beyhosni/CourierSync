import React, { useRef, Suspense } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, Grid, Sparkles, Line } from '@react-three/drei';
import * as THREE from 'three';
import { DeliveryPoint, DeliveryRoute } from '../../types/delivery';

interface DeliveryMap3DProps {
  deliveryPoints: DeliveryPoint[];
  deliveryRoutes: DeliveryRoute[];
  currentDriverPosition?: { lat: number; lng: number };
  viewMode?: 'driver' | 'dispatcher' | 'customer';
  onPointClick?: (point: DeliveryPoint) => void;
}

// Convert lat/lng to 3D coordinates (remains a utility function)
const latLngToVector3 = (lat: number, lng: number, height: number = 0): [number, number, number] => {
  const radius = 100; // Earth radius in our 3D world
  const phi = (90 - lat) * (Math.PI / 180);
  const theta = (lng + 180) * (Math.PI / 180);

  const x = -(radius * Math.sin(phi) * Math.cos(theta));
  const z = radius * Math.sin(phi) * Math.sin(theta);
  const y = radius * Math.cos(phi);

  return [x, y + height, z];
};


const DeliveryPointMarker: React.FC<{ point: DeliveryPoint; onClick?: (point: DeliveryPoint) => void }> = ({ point, onClick }) => {
    const meshRef = useRef<THREE.Mesh>(null!);
  
    useFrame(({ clock }) => {
      const pulseScale = 1 + Math.sin(clock.getElapsedTime() * 2) * 0.2;
      if (meshRef.current) {
        meshRef.current.scale.set(pulseScale, pulseScale, pulseScale);
      }
    });
  
    return (
      <mesh
        ref={meshRef}
        position={latLngToVector3(point.lat, point.lng, point.type === 'pickup' ? 2 : 1)}
        castShadow
        receiveShadow
        onClick={() => onClick?.(point)}
      >
        <sphereGeometry args={[1, 16, 16]} />
        <meshPhongMaterial
          color={point.type === 'pickup' ? '#00ff00' : '#ff0000'}
          emissive={point.type === 'pickup' ? '#00cc00' : '#cc0000'}
          emissiveIntensity={0.6}
        />
      </mesh>
    );
};

const DriverMarker: React.FC<{ position: { lat: number, lng: number } }> = ({ position }) => {
    const meshRef = useRef<THREE.Mesh>(null!);
  
    useFrame(() => {
        if (meshRef.current) {
            meshRef.current.rotation.y += 0.01;
        }
    });

    return (
      <mesh
        ref={meshRef}
        position={latLngToVector3(position.lat, position.lng, 3)}
        castShadow
        receiveShadow
      >
        <coneGeometry args={[1.2, 2.5, 8]} />
        <meshPhongMaterial
          color="#ffff00"
          emissive="#ffff00"
          emissiveIntensity={0.8}
        />
        <Sparkles count={20} scale={3} size={6} speed={0.4} />
      </mesh>
    );
};
  
const RouteLine: React.FC<{ route: DeliveryRoute }> = ({ route }) => {
    const points = route.points.map(p => new THREE.Vector3(...latLngToVector3(p.lat, p.lng, 0.5)));
    
    return <Line points={points} color="#0088ff" lineWidth={2} dashed={false} />;
};


const Scene: React.FC<DeliveryMap3DProps> = ({ deliveryPoints, deliveryRoutes, currentDriverPosition, onPointClick }) => {
    return (
        <>
            <ambientLight intensity={0.3} />
            <directionalLight
                position={[50, 100, 50]}
                intensity={0.7}
                castShadow
                shadow-camera-near={0.1}
                shadow-camera-far={500}
                shadow-camera-left={-100}
                shadow-camera-right={100}
                shadow-camera-top={100}
                shadow-camera-bottom={-100}
            />
            <Grid
                position={[0, -0.01, 0]}
                args={[200, 20]}
                cellColor="#444444"
                sectionColor="#222222"
            />
            
            {deliveryPoints.map(point => (
                <DeliveryPointMarker key={point.id} point={point} onClick={onPointClick} />
            ))}

            {deliveryRoutes.map(route => (
                <RouteLine key={route.id} route={route} />
            ))}

            {currentDriverPosition && <DriverMarker position={currentDriverPosition} />}
        </>
    );
}

const DeliveryMap3D: React.FC<DeliveryMap3DProps> = (props) => {
  return (
    <div className="w-full h-full">
        <Canvas
            shadows
            camera={{ position: [0, 50, 120], fov: 75 }}
        >
            <color attach="background" args={[0x050505]} />
            <fog attach="fog" args={[0x050505, 10, 500]} />
            <Suspense fallback={null}>
                <Scene {...props} />
            </Suspense>
            <OrbitControls
                enableDamping
                dampingFactor={0.05}
                minDistance={10}
                maxDistance={200}
            />
        </Canvas>
    </div>
  );
};

export default DeliveryMap3D;
