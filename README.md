# `tote` minimal K8s plugin

***

>Tote is a minimal K8s plugin to perform bulk operations against K8s Pods and Services.


| Steps to install `tote`: |
|-----------------------------------------------------------------------------------------------------------------------------|
| 1. `cp tote.java /usr/local/bin/tote` <br/>
| 2. `chmod +x /usr/local/bin/tote`                |
***
| How to run `tote` |
|-----------------------------------------------------------------------------------------------------------------------------|
| 1. `tote -h`: to see usage<br/>
| 2. `tote`: to get info about pods and services of all namespaces<br/>
| 3. `tote -n <namespace>` : to get info about pods and services of a specific namespace<br/>
| 4. `tote -l <label>` : to get info about pods and services of a specific label key               
| 5. `tote -P`: to get info about nodes and namespaces as well, along with pods and services |
***
## Examples of usage:

```bash
user@laptop: ~ $ tote -n oss
________________________________________________________________________________________________________________________________________________________________________
|       | Kind| Namespace| Name                            | State  | Image(s)                                                                | Node Name               |
|=======================================================================================================================================================================|
| ✓     | Pod | oss      | oss-api-gateway-6565466df8-tqr25| RUNNING| registry.ubitech.eu/nsit/operations-support-system/oss-api-gateway:0.8.0| k3d-oss-cluster-server-0|
| ✓     | Pod | oss      | kafka-0                         | RUNNING| vectorized/redpanda:latest                                              | k3d-oss-cluster-server-0|
| ✓     | Pod | oss      | oss-kibana-kb-6d66787d9f-thqzd  | RUNNING| docker.elastic.co/kibana/kibana:8.2.2                                   | k3d-oss-cluster-server-0|
| ✓     | Pod | oss      | oss-slicing-6945f59455-t92cp    | RUNNING| registry.ubitech.eu/nsit/operations-support-system/oss-slicing:0.11.1   | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-elasticsearch-es-default-0  | RUNNING| docker.elastic.co/elasticsearch/elasticsearch:8.2.2                     | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-core-848b96bd7c-g4wv5       | RUNNING| registry.ubitech.eu/nsit/operations-support-system/oss-core:0.9.0       | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-filebeat-beat-filebeat-clfvq| RUNNING| docker.elastic.co/beats/filebeat:8.2.2                                  | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-filebeat-beat-filebeat-x9ghd| RUNNING| docker.elastic.co/beats/filebeat:8.2.2                                  | k3d-oss-cluster-server-0|
___________________________________________________________________________________________________________________
|  | Kind   | Namespace| Name                              | Type     | IP           | Ports                       |
|==================================================================================================================|
| ⎈| Service| oss      | oss-core                          | ClusterIP| 10.43.19.33  | 8080/TCP                    |
| ⎈| Service| oss      | kafka                             | ClusterIP| None         | 9644/TCP, 9092/TCP, 8082/TCP|
| ⎈| Service| oss      | oss-slicing                       | ClusterIP| 10.43.253.62 | 8080/TCP                    |
| ⎈| Service| oss      | oss-api-gateway                   | NodePort | 10.43.30.65  | 8080:30925/TCP              |
| ⎈| Service| oss      | oss-elasticsearch-es-transport    | ClusterIP| None         | 9300/TCP                    |
| ⎈| Service| oss      | oss-elasticsearch-es-http         | ClusterIP| 10.43.200.137| 9200/TCP                    |
| ⎈| Service| oss      | oss-elasticsearch-es-internal-http| ClusterIP| 10.43.17.226 | 9200/TCP                    |
| ⎈| Service| oss      | oss-kibana-kb-http                | ClusterIP| 10.43.145.106| 5601/TCP                    |
| ⎈| Service| oss      | oss-elasticsearch-es-default      | ClusterIP| None         | 9200/TCP                    |

```
***

```bash
user@laptop: ~ $ tote -n oss -P
__________________________________________________________________________
|       | Kind| Name                    | Status| INTERNAL-IP| EXTERNAL-IP|
|=========================================================================|
| ✓     | Node| k3d-oss-cluster-server-0| Ready | 172.18.0.3 | <none>     |
| ✓     | Node| k3d-oss-cluster-agent-0 | Ready | 172.18.0.2 | <none>     |
____________________________________________
|       | Kind     | Name           | Status|
|===========================================|
| ✓     | Namespace| default        | Active|
| ✓     | Namespace| kube-system    | Active|
| ✓     | Namespace| kube-public    | Active|
| ✓     | Namespace| kube-node-lease| Active|
| ✓     | Namespace| oss            | Active|
| ✓     | Namespace| cert-manager   | Active|
| ✓     | Namespace| redpanda-system| Active|
| ✓     | Namespace| elastic-system | Active|
________________________________________________________________________________________________________________________________________________________________________
|       | Kind| Namespace| Name                            | State  | Image(s)                                                                | Node Name               |
|=======================================================================================================================================================================|
| ✓     | Pod | oss      | oss-api-gateway-6565466df8-tqr25| RUNNING| registry.ubitech.eu/nsit/operations-support-system/oss-api-gateway:0.8.0| k3d-oss-cluster-server-0|
| ✓     | Pod | oss      | kafka-0                         | RUNNING| vectorized/redpanda:latest                                              | k3d-oss-cluster-server-0|
| ✓     | Pod | oss      | oss-kibana-kb-6d66787d9f-thqzd  | RUNNING| docker.elastic.co/kibana/kibana:8.2.2                                   | k3d-oss-cluster-server-0|
| ✓     | Pod | oss      | oss-slicing-6945f59455-t92cp    | RUNNING| registry.ubitech.eu/nsit/operations-support-system/oss-slicing:0.11.1   | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-elasticsearch-es-default-0  | RUNNING| docker.elastic.co/elasticsearch/elasticsearch:8.2.2                     | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-core-848b96bd7c-g4wv5       | RUNNING| registry.ubitech.eu/nsit/operations-support-system/oss-core:0.9.0       | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-filebeat-beat-filebeat-clfvq| RUNNING| docker.elastic.co/beats/filebeat:8.2.2                                  | k3d-oss-cluster-agent-0 |
| ✓     | Pod | oss      | oss-filebeat-beat-filebeat-x9ghd| RUNNING| docker.elastic.co/beats/filebeat:8.2.2                                  | k3d-oss-cluster-server-0|
___________________________________________________________________________________________________________________
|  | Kind   | Namespace| Name                              | Type     | IP           | Ports                       |
|==================================================================================================================|
| ⎈| Service| oss      | oss-core                          | ClusterIP| 10.43.19.33  | 8080/TCP                    |
| ⎈| Service| oss      | kafka                             | ClusterIP| None         | 9644/TCP, 9092/TCP, 8082/TCP|
| ⎈| Service| oss      | oss-slicing                       | ClusterIP| 10.43.253.62 | 8080/TCP                    |
| ⎈| Service| oss      | oss-api-gateway                   | NodePort | 10.43.30.65  | 8080:30925/TCP              |
| ⎈| Service| oss      | oss-elasticsearch-es-transport    | ClusterIP| None         | 9300/TCP                    |
| ⎈| Service| oss      | oss-elasticsearch-es-http         | ClusterIP| 10.43.200.137| 9200/TCP                    |
| ⎈| Service| oss      | oss-elasticsearch-es-internal-http| ClusterIP| 10.43.17.226 | 9200/TCP                    |
| ⎈| Service| oss      | oss-kibana-kb-http                | ClusterIP| 10.43.145.106| 5601/TCP                    |
| ⎈| Service| oss      | oss-elasticsearch-es-default      | ClusterIP| None         | 9200/TCP                    |


```



