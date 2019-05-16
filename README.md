# yaegar-rest-service

sudo docker stop yaegar-rest-service || true && sudo docker rm yaegar-rest-service || true && sudo docker rmi -f com.yaegar/yaegar-rest-service || true && sudo docker build -t com.yaegar/yaegar-rest-service . && sudo docker run -p 8888:8080 --name yaegar-rest-service --link postgres -t -e app.db.host=postgres com.yaegar/yaegar-rest-service
