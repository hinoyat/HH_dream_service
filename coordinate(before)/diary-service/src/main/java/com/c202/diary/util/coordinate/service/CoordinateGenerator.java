package com.c202.diary.util.coordinate.service;

import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.util.coordinate.model.ClusterDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class CoordinateGenerator {

    private final Random random = new Random();

    private static final double DEFAULT_CLUSTER_RADIUS = 10.0;
    private static final double MAX_CLUSTER_RADIUS = 20.0;
    private static final double COLLISION_MIN_DISTANCE = 5.0;
    private static final int MAX_ADJUSTMENT_ATTEMPTS = 10;

    public double[] generateCoordinatesInCluster(ClusterDto cluster, Emotion emotion) {
        double clusterRadius = DEFAULT_CLUSTER_RADIUS;
        if (!cluster.getDiaries().isEmpty()) {
            clusterRadius = Math.min(MAX_CLUSTER_RADIUS, 5.0 + 2.0 * cluster.getDiaries().size());
        }

        double distance = clusterRadius * random.nextDouble();
        double theta = random.nextDouble() * 2 * Math.PI;
        double phi = random.nextDouble() * Math.PI;
        double offsetX = distance * Math.sin(phi) * Math.cos(theta);
        double offsetY = distance * Math.sin(phi) * Math.sin(theta);
        double offsetZ = distance * Math.cos(phi);

        double[] coordinates = new double[3];
        coordinates[0] = cluster.getCenterX() + offsetX;
        coordinates[1] = cluster.getCenterY() + offsetY;
        coordinates[2] = cluster.getCenterZ() + offsetZ;

        // 감정 영역 내에 있도록 조정
        double distanceFromEmotionCenter = calculateDistance(
                emotion.getBaseX(), emotion.getBaseY(), emotion.getBaseZ(),
                coordinates[0], coordinates[1], coordinates[2]
        );
        if (distanceFromEmotionCenter > emotion.getBaseRadius()) {
            double scale = emotion.getBaseRadius() / distanceFromEmotionCenter * 0.95;
            coordinates[0] = emotion.getBaseX() + (coordinates[0] - emotion.getBaseX()) * scale;
            coordinates[1] = emotion.getBaseY() + (coordinates[1] - emotion.getBaseY()) * scale;
            coordinates[2] = emotion.getBaseZ() + (coordinates[2] - emotion.getBaseZ()) * scale;
        }
        return coordinates;
    }

    public double[] adjustCoordinates(double[] coordinates, List<double[]> existingCoordinates) {
        double[] adjusted = coordinates.clone();
        int maxIterations = 20;  // 최대 반복 횟수
        double minDistance = COLLISION_MIN_DISTANCE;  // 최소 충돌 간격 (예: 5.0)
        double tolerance = 0.01;  // 반복 종료 기준 (총 조정량이 이보다 작으면 종료)

        for (int iter = 0; iter < maxIterations; iter++) {
            double totalAdjustment = 0.0;
            // 모든 기존 좌표와 비교하여 충돌 체크
            for (double[] other : existingCoordinates) {
                double dx = adjusted[0] - other[0];
                double dy = adjusted[1] - other[1];
                double dz = adjusted[2] - other[2];
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                // 충돌 발생 시 (거리가 최소 요구 거리보다 작은 경우)
                if (dist < minDistance) {
                    double overlap = minDistance - dist;
                    // 단위 벡터 계산 (dist가 0이면 무작위 벡터 선택)
                    if (dist == 0) {
                        dx = (random.nextDouble() - 0.5);
                        dy = (random.nextDouble() - 0.5);
                        dz = (random.nextDouble() - 0.5);
                        dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    }
                    double ux = dx / dist;
                    double uy = dy / dist;
                    double uz = dz / dist;
                    // 전체 오버랩의 절반 정도만 이동 (점진적 조정)
                    double adjustment = overlap * 0.5;
                    adjusted[0] += ux * adjustment;
                    adjusted[1] += uy * adjustment;
                    adjusted[2] += uz * adjustment;
                    totalAdjustment += adjustment;
                }
            }
            // 총 조정량이 작으면 반복 종료
            if (totalAdjustment < tolerance) {
                break;
            }
        }
        return adjusted;
    }


    public double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2)
                + Math.pow(y2 - y1, 2)
                + Math.pow(z2 - z1, 2));
    }
}
