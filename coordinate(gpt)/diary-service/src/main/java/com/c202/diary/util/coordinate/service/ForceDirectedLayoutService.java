package com.c202.diary.util.coordinate.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ForceDirectedLayoutService {

    // 물리 시뮬레이션 관련 상수 (필요에 따라 조정)
    private static final double REPULSION_CONSTANT = 1000.0;
    private static final double ATTRACTION_CONSTANT = 0.1;
    private static final int ITERATIONS = 50;
    private static final double DAMPING = 0.85;
    private static final double MIN_DISTANCE_EPSILON = 0.01; // 0에 가까운 값 방지

    private final Random random = new Random();

    /**
     * 클러스터 내 여러 노드의 초기 좌표 배열과 클러스터 중심을 입력받아,
     * Force-Directed Layout 알고리즘을 적용한 후 새로운 좌표 배열을 반환합니다.
     *
     * @param positions     [n][3] 형태의 초기 좌표 배열
     * @param clusterCenter 클러스터 중심 좌표 (배열 길이 3)
     * @return 재배치된 좌표 배열
     */
    public double[][] applyForceDirectedLayout(double[][] positions, double[] clusterCenter) {
        int n = positions.length;
        double[][] velocities = new double[n][3]; // 초기 속도는 모두 0

        // 지정된 횟수만큼 반복하여 힘을 적용
        for (int iter = 0; iter < ITERATIONS; iter++) {
            // 각 노드별로 힘 계산
            for (int i = 0; i < n; i++) {
                double forceX = 0, forceY = 0, forceZ = 0;
                // 다른 노드들과의 상호작용 (반발력)
                for (int j = 0; j < n; j++) {
                    if (i == j) continue;
                    double dx = positions[i][0] - positions[j][0];
                    double dy = positions[i][1] - positions[j][1];
                    double dz = positions[i][2] - positions[j][2];
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance < MIN_DISTANCE_EPSILON) distance = MIN_DISTANCE_EPSILON;  // 너무 가까워지지 않게 함
                    double repulsion = REPULSION_CONSTANT / (distance * distance);
                    forceX += (dx / distance) * repulsion;
                    forceY += (dy / distance) * repulsion;
                    forceZ += (dz / distance) * repulsion;
                }
                // 클러스터 중심으로의 끌림 (인력)
                double dxCenter = clusterCenter[0] - positions[i][0];
                double dyCenter = clusterCenter[1] - positions[i][1];
                double dzCenter = clusterCenter[2] - positions[i][2];
                double distCenter = Math.sqrt(dxCenter * dxCenter + dyCenter * dyCenter + dzCenter * dzCenter);
                if (distCenter < MIN_DISTANCE_EPSILON) distCenter = MIN_DISTANCE_EPSILON;
                double attraction = ATTRACTION_CONSTANT * distCenter;
                forceX += (dxCenter / distCenter) * attraction;
                forceY += (dyCenter / distCenter) * attraction;
                forceZ += (dzCenter / distCenter) * attraction;

                // 업데이트: 현재 속도에 힘을 더하고 감쇠 효과 적용
                velocities[i][0] = (velocities[i][0] + forceX) * DAMPING;
                velocities[i][1] = (velocities[i][1] + forceY) * DAMPING;
                velocities[i][2] = (velocities[i][2] + forceZ) * DAMPING;
            }
            // 속도에 따라 위치 업데이트
            for (int i = 0; i < n; i++) {
                positions[i][0] += velocities[i][0];
                positions[i][1] += velocities[i][1];
                positions[i][2] += velocities[i][2];
            }
        }
        return positions;
    }
}
