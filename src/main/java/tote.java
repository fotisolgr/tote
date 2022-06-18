///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS io.fabric8:kubernetes-client:5.12.2
//DEPS org.bouncycastle:bcpkix-jdk15on:1.58
//DEPS com.massisframework:j-text-utils:0.3.4

import dnl.utils.text.table.TextTable;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.PodStatusUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "tote", mixinStandardHelpOptions = true, version = "tote 0.1",
        description = "list pods and services in given namespace")
class tote implements Callable<Integer> {

    private static final String SIX_SPACES = "      ";
    private static final String CHECK_MARK = "\u2713";
    private static final String CROSS_MARK = "\u2716";
    private static final String GEAR = "\u2388";

    @CommandLine.Option(
            names = {"-n", "--namespace"},
            description = "The namespace to fetch pods and services info"
    )
    private static String namespace;

    @CommandLine.Option(
            names = {"-l", "--label"},
            description = "The associated label to fetch pods and services info"
    )
    private static String label;

    @CommandLine.Option(
            names = {"-P", "--print-nodes-and-namespaces"},
            description = "Print useful info about nodes and namespaces"
    )
    private static boolean isPrint;

    public static void main(String... args) {
        int exitCode = new CommandLine(new tote()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try (KubernetesClient k8sClient = new DefaultKubernetesClient()) {
            if (isPrint) {
                printNodesTable(getNodesInfo(k8sClient));
                printNamespacesTable(getNamespaceInfo(k8sClient));
            }
            printPodsTable(getPodsInfo(k8sClient));
            printServicesTable(getServicesInfo(k8sClient));
            return 0;

        } catch (Exception e) {
            throw new IllegalStateException("Kubernetes client could not be initialized", e);
        }
    }

    private static List<PodInfo> getPodsInfo(KubernetesClient k8sClient) {
        if (namespace != null && label != null) {
            return k8sClient.pods().inNamespace(namespace).withLabel(label).list().getItems()
                    .stream()
                    .map(pod -> getPodInfo(k8sClient, pod)).collect(Collectors.toList());
        } else if (namespace != null) {
            return k8sClient.pods().inNamespace(namespace).list().getItems()
                    .stream()
                    .map(pod -> getPodInfo(k8sClient, pod)).collect(Collectors.toList());
        } else if (label != null) {
            return k8sClient.pods().withLabel(label).list().getItems()
                    .stream()
                    .map(pod -> getPodInfo(k8sClient, pod)).collect(Collectors.toList());
        }

        return k8sClient.pods().list().getItems().stream()
                .map(pod -> getPodInfo(k8sClient, pod)).collect(Collectors.toList());
    }

    private static PodInfo getPodInfo(KubernetesClient k8sClient, Pod pod) {
        final var kind = pod.getKind();
        final var state =
                PodStatusUtil.isRunning(pod) ? PodInfoState.RUNNING : PodInfoState.FAILING;
        final var namespace = pod.getMetadata().getNamespace();
        final var images = pod.getSpec().getContainers().stream().map(Container::getImage)
                .collect(Collectors.toList());
        final var nodeName = pod.getSpec().getNodeName();

        k8sClient.close();

        return new PodInfo(kind, namespace, pod.getMetadata().getName(), state, images, nodeName);
    }

    private static List<ServiceInfo> getServicesInfo(KubernetesClient k8sClient) {
        if (namespace != null) {
            return k8sClient.services().inNamespace(namespace).list().getItems().stream()
                    .map(service -> getServiceInfo(k8sClient, service))
                    .collect(Collectors.toList());
        }

        return k8sClient.services().list().getItems().stream()
                .map(service -> getServiceInfo(k8sClient, service))
                .collect(Collectors.toList());
    }

    private static ServiceInfo getServiceInfo(KubernetesClient k8sClient,
            Service service) {
        List<String> ports = new ArrayList<>();

        final var kind = service.getKind();
        final var namespace = service.getMetadata().getNamespace();
        final var name = service.getMetadata().getName();
        final var type = service.getSpec().getType();
        final var clusterIp = service.getSpec().getClusterIP();
        final var servicePorts = service.getSpec().getPorts();

        for (ServicePort servicePort : servicePorts) {
            final var port = servicePort.getPort();
            final var protocol = servicePort.getProtocol();

            if (type.equals("NodePort")) {
                final var nodePort = servicePort.getNodePort();
                ports.add(port + ":" + nodePort + "/" + protocol);

                return new ServiceInfo(kind, namespace, name, type, clusterIp, ports);
            }
            ports.add(port + "/" + protocol);
        }

        k8sClient.close();

        return new ServiceInfo(kind, namespace, name, type, clusterIp, ports);
    }

    private static List<NodeInfo> getNodesInfo(KubernetesClient k8sClient) {
        return k8sClient.nodes().list().getItems().stream()
                .map(node -> getNodeInfo(k8sClient, node))
                .collect(Collectors.toList());
    }

    private static NodeInfo getNodeInfo(KubernetesClient k8sClient,
            Node node) {

        final var kind = node.getKind();
        final var name = node.getMetadata().getName();

        String status = "";
        String internalIp = "";
        String externalIp = "<none>";

        final var nodeConditions = new ArrayList<>(node.getStatus().getConditions());
        final var nodeAddresses = new ArrayList<>(node.getStatus().getAddresses());

        for (NodeAddress nodeAddress : nodeAddresses) {
            final var type = nodeAddress.getType();

            if (type.equals("InternalIP")) {
                internalIp = nodeAddress.getAddress();
            } else if (type.equals("ExternalIP")) {
                externalIp = nodeAddress.getAddress();
            }
        }

        for (NodeCondition nodeCondition : nodeConditions) {
            status = nodeCondition.getType();
        }

        k8sClient.close();

        return new NodeInfo(kind, name, status, internalIp, externalIp);
    }

    private static List<NamespaceInfo> getNamespaceInfo(KubernetesClient k8sClient) {
        List<NamespaceInfo> namespaceInfos = new ArrayList<>();

        final var namespaces = k8sClient.namespaces().list().getItems();

        for (Namespace namespace : namespaces) {
            final var kind = namespace.getKind();
            final var name = namespace.getMetadata().getName();
            final var status = namespace.getStatus().getPhase();

            final var namespaceInfo = new NamespaceInfo(kind, name, status);
            namespaceInfos.add(namespaceInfo);
        }
        return namespaceInfos;
    }

    private static void printPodsTable(List<PodInfo> list) {
        final Object[][] tableData = list.stream()
                .map(podInfo -> new Object[]{
                        podInfo.getState().equals(PodInfoState.RUNNING) ? CHECK_MARK : CROSS_MARK,
                        podInfo.getKind(),
                        podInfo.getNamespace(),
                        podInfo.getName(),
                        podInfo.getState(),
                        toString(podInfo.getImages()),
                        podInfo.getNodeName()
                })
                .toArray(Object[][]::new);
        String[] columnNames = {SIX_SPACES, "Kind", "Namespace", "Name", "State", "Image(s)",
                "Node Name"};
        TextTable tt = new TextTable(columnNames, tableData);

        tt.printTable();
    }

    private static void printServicesTable(List<ServiceInfo> list) {
        final Object[][] tableData = list.stream()
                .map(serviceInfo -> new Object[]{
                        GEAR,
                        serviceInfo.getKind(),
                        serviceInfo.getNamespace(),
                        serviceInfo.getName(),
                        serviceInfo.getType(),
                        serviceInfo.getClusterIp(),
                        toString(serviceInfo.getPorts())
                })
                .toArray(Object[][]::new);

        String[] columnNames = {"", "Kind", "Namespace", "Name", "Type", "IP", "Ports"};
        TextTable tt = new TextTable(columnNames, tableData);

        tt.printTable();
    }

    private static void printNodesTable(List<NodeInfo> list) {
        final Object[][] tableData = list.stream()
                .map(nodeInfo -> new Object[]{
                        nodeInfo.getStatus().equals("Ready") ? CHECK_MARK : CROSS_MARK,
                        nodeInfo.getKind(),
                        nodeInfo.getName(),
                        nodeInfo.getStatus(),
                        nodeInfo.getInternalIp(),
                        nodeInfo.getExternalIp(),
                })
                .toArray(Object[][]::new);

        String[] columnNames = {SIX_SPACES, "Kind", "Name", "Status", "INTERNAL-IP",
                "EXTERNAL-IP"};
        TextTable tt = new TextTable(columnNames, tableData);

        tt.printTable();
    }

    private static void printNamespacesTable(List<NamespaceInfo> list) {
        final Object[][] tableData = list.stream()
                .map(namespaceInfo -> new Object[]{
                        namespaceInfo.getStatus().equals("Active") ? CHECK_MARK : CROSS_MARK,
                        namespaceInfo.getKind(),
                        namespaceInfo.getName(),
                        namespaceInfo.getStatus(),
                })
                .toArray(Object[][]::new);

        String[] columnNames = {SIX_SPACES, "Kind", "Name", "Status"};
        TextTable tt = new TextTable(columnNames, tableData);

        tt.printTable();
    }

    private static <T> String toString(List<T> list) {

        return list.stream().map(T::toString).collect(Collectors.joining(", "));
    }

    // Object Classes
    static class PodInfo {

        private final String kind;
        private final String namespace;
        private final String name;
        private final PodInfoState state;
        private final List<String> images;
        private final String nodeName;

        public PodInfo(String kind, String namespace, String name, PodInfoState state,
                List<String> images, String nodeName) {
            this.kind = kind;
            this.namespace = namespace;
            this.name = name;
            this.state = state;
            this.images = images;
            this.nodeName = nodeName;
        }

        public String getKind() {
            return kind;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getName() {
            return name;
        }

        public PodInfoState getState() {
            return state;
        }

        public List<String> getImages() {
            return images;
        }

        public String getNodeName() {
            return nodeName;
        }
    }

    enum PodInfoState {
        RUNNING,
        FAILING
    }

    static class ServiceInfo {

        private final String kind;
        private final String namespace;
        private final String name;
        private final String type;
        private final String clusterIp;
        private final List<String> ports;

        public ServiceInfo(String kind, String namespace, String name, String type,
                String clusterIp,
                List<String> ports) {
            this.kind = kind;
            this.namespace = namespace;
            this.name = name;
            this.type = type;
            this.clusterIp = clusterIp;
            this.ports = ports;
        }

        public String getKind() {
            return kind;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getClusterIp() {
            return clusterIp;
        }

        public List<String> getPorts() {
            return ports;
        }
    }

    static class NodeInfo {

        private final String kind;
        private final String name;
        private final String status;
        private final String internalIp;
        private final String externalIp;

        public NodeInfo(String kind, String name, String status,
                String internalIp,
                String externalIp) {
            this.kind = kind;
            this.name = name;
            this.status = status;
            this.internalIp = internalIp;
            this.externalIp = externalIp;
        }

        public String getKind() {
            return kind;
        }

        public String getName() {
            return name;
        }

        public String getStatus() {
            return status;
        }

        public String getInternalIp() {
            return internalIp;
        }

        public String getExternalIp() {
            return externalIp;
        }
    }

    static class NamespaceInfo {

        private final String kind;
        private final String name;
        private final String status;

        public NamespaceInfo(String kind, String name,
                String status) {
            this.kind = kind;
            this.name = name;
            this.status = status;
        }

        public String getKind() {
            return kind;
        }

        public String getName() {
            return name;
        }

        public String getStatus() {
            return status;
        }
    }
}