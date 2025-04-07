import {
  Object3DNode,
  MaterialNode,
  BufferGeometryNode,
  LightNode,
} from '@react-three/fiber';
import * as THREE from 'three';

declare global {
  namespace JSX {
    interface IntrinsicElements {
      group: Object3DNode<THREE.Group, typeof THREE.Group>;
      mesh: Object3DNode<THREE.Mesh, typeof THREE.Mesh>;
      pointLight: Object3DNode<THREE.PointLight, typeof THREE.PointLight>;
      ambientLight: LightNode<THREE.AmbientLight, typeof THREE.AmbientLight>;
      sphereGeometry: BufferGeometryNode<
        THREE.SphereGeometry,
        typeof THREE.SphereGeometry
      >;
      meshStandardMaterial: MaterialNode<
        THREE.MeshStandardMaterial,
        typeof THREE.MeshStandardMaterial
      >;
    }
  }
}
