# yaegar-rest-service

sudo docker stop yaegar-rest-service || true && sudo docker rm yaegar-rest-service || true && sudo docker rmi -f com.yaegar/yaegar-rest-service || true && sudo docker build -t com.yaegar/yaegar-rest-service . && sudo docker run -p 8888:8080 --name yaegar-rest-service --link mariadb -t -e app.db.host=mariadb com.yaegar/yaegar-rest-service
