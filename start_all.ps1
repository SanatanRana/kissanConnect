Write-Host "Starting Discovery Service..."
Start-Process powershell -ArgumentList "-NoExit","-Command","cd discovery-service; .\mvnw spring-boot:run"
Start-Sleep -Seconds 15

Write-Host "Starting API Gateway and Microservices..."
Start-Process powershell -ArgumentList "-NoExit","-Command","cd api-gateway; .\mvnw spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit","-Command","cd user-service; .\mvnw spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit","-Command","cd product-service; .\mvnw spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit","-Command","cd order-service; .\mvnw spring-boot:run"

Write-Host "Starting Frontend..."
Start-Process powershell -ArgumentList "-NoExit","-Command","cd kissan-connect-frontend; npm install; npm run dev"
