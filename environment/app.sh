docker stop quarkus-person
docker run -it --name quarkus-person --network host -p 8080:8080 alissonvisa/code-with-quarkus:1.0.0-SNAPSHOT