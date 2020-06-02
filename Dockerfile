FROM repo.d.k8s/cx_java8:v1.0
COPY . /
CMD ["java", "-jar", "libs/rm-web-0.0.1.jar"]
