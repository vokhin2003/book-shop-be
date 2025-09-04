#!/bin/bash

echo "🗄️ Testing HikariCP Connection Pool..."

echo "📊 Initial pool status:"
curl -s http://localhost:8080/api/v1/health/database | jq .

echo -e "\n🔄 Creating concurrent load (10 requests)..."

# Create 10 concurrent requests to trigger multiple connections
for i in {1..10}; do
    curl -s http://localhost:8080/api/v1/users &
done

sleep 2

echo -e "\n📊 Pool status under load:"
curl -s http://localhost:8080/api/v1/health/database | jq .

wait

echo -e "\n📊 Final pool status:"
curl -s http://localhost:8080/api/v1/health/database | jq .

echo -e "\n✅ Connection pool test completed!"
