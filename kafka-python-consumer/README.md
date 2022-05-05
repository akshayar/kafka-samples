## Build
```shell
docker build -t arawa3/kafka-python-consumer:0.0.1 .
docker push arawa3/kafka-python-consumer:0.0.1
```
## Run
```shell
docker run kafka-python-consumer:0.0.1 <topic> <group> <bootstreap>
docker run kafka-python-consumer:0.0.1 data-json group1 ip-192-168-41-165.ap-south-1.compute.internal:9092
```

## Run From Kubernetes
```shell
kubectl apply -f config.yaml  
kubectl get pods

## Deploy KEDA
helm repo add kedacore https://kedacore.github.io/charts
helm repo update 
kubectl create namespace keda                           
helm install keda kedacore/keda --namespace keda
kubectl get crd | grep keda
kubectl apply -f keda.yaml 
```


