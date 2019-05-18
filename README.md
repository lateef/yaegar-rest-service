# yaegar-rest-service

sudo docker stop yaegar-rest-service || true && sudo docker rm yaegar-rest-service || true && sudo docker rmi -f com.yaegar/yaegar-rest-service || true && sudo docker build -t com.yaegar/yaegar-rest-service . && sudo docker run -p 8888:8080 --name yaegar-rest-service --link postgres -t -e app.db.host=postgres com.yaegar/yaegar-rest-service

sudo docker stop postgres || true && sudo docker rm postgres || true && sudo docker run --name postgres -p5432:5432 -e POSTGRES_USER=yaegaruser -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=yaegardb -d postgres

with a Dockerfile:

FROM library/postgres
ENV POSTGRES_USER docker
ENV POSTGRES_PASSWORD docker
ENV POSTGRES_DB docker
