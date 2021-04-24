cd ..
mvn package -Pnative -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-native-image:21.0-java11 -Dquarkus.native.container-build=true