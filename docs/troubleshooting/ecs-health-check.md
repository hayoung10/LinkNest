# ECS 배포 시 Security Group 설정 오류로 인한 Health Check 실패 및 Rollback

## 문제 사항

GitHub Actions을 통해 Staging ECS(Fargate) 배포를 진행하던 중 Health Check 실패로 배포가 정상적으로 완료되지 않는 문제가 발생했다.

- ECS Task가 실행된 후 반복 종료

- ALB Target Group 상태가 `Unhealthy`로 변경

- Health Check 요청 시간 초과

- ECS Deployment Rollback 발생

주요 오류 메시지는 다음과 같다.

```text
port 8080 is unhealthy due to Health checks failed
Request timed out
```

ECS Service Event에서도 다음 메시지가 확인되었다.

```text
task failed ELB health checks in target-group
```

종료된 Task에서는 `exit code: 143`이 확인되었다.

<br>

## 원인 분석

문제는 애플리케이션이 아니라 ECS Security Group의 인바운드 규칙 설정 오류였다.

당시 ECS Security Group의 인바운드 규칙이 다음과 같이 설정되어 있었다.

```text
ECS SG ← ECS SG (self)
```

이로 인해 ALB에서 ECS로 전달되는 트래픽이 차단되었다.

Health Check 요청이 정상적으로 전달되지 않으면서 다음과 같은 상황이 반복되었다.

1. ALB가 ECS의 `/actuator/health`로 Health Check 요청
2. Security Group 설정 오류로 요청이 ECS에 전달되지 않음
3. Target Group에서 `Request timed out` 및 `Unhealthy` 판정
4. ECS가 해당 Task를 비정상 상태로 판단하고 종료
5. 새로운 Task 생성 후 동일한 문제 반복
6. 배포가 정상적으로 안정화되지 못하고 Rollback

<br>

## 해결

ECS Security Group의 인바운드 규칙을 다음과 같이 수정했다.

```text
ECS SG ← ALB SG (TCP 8080)
```

즉, ECS의 8080 포트에 대해 ALB Security Group에서 들어오는 트래픽만 허용하도록 변경했다.

<br>

## 검증

설정 수정 후 다음 사항을 확인했다.

- ALB Target Group 상태가 `Healthy`로 변경

- ECS Task가 정상적으로 실행 및 유지

- `/actuator/health` 요청에 정상 응답 (`{"status":"UP"}`)

- Staging ECS 배포 정상 완료

<br>

## 배운 점

- ALB Health Check 실패 시 애플리케이션 상태뿐 아니라 ALB와 ECS 간 네트워크 연결 및 Security Group 설정도 확인해야 한다.

- ALB Health Check의 `Request timed out`은 네트워크 연결 문제일 가능성도 있으므로 트래픽 흐름을 확인해야 한다.

- ECS는 Health Check 결과와 배포 안정화 상태를 바탕으로 Task를 교체하거나 배포를 Rollback할 수 있다.
