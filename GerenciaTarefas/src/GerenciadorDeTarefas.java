import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Scanner;
import java.util.Set;
import java.util.Map;

public class GerenciadorDeTarefas {

    private final JedisPool jedisPool;

    public GerenciadorDeTarefas() {
        JedisPoolConfig configDoPool = new JedisPoolConfig();
        this.jedisPool = new JedisPool(configDoPool, "localhost", 6379);
    }

    public String adicionarTarefa(String descricao) {
        try (Jedis jedis = jedisPool.getResource()) {
            long id = jedis.incr("contadorDeIds");
            String idStr = Long.toString(id);
            jedis.hset("tarefa:" + idStr, "descricao", descricao);
            return idStr;
        }
    }

    public void listarTarefas() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys("tarefa:*");
            System.out.println("Lista de Tarefas:");
            for (String key : keys) {
                if (jedis.type(key).equals("hash")) {
                    String id = key.split(":")[1];
                    String descricao = jedis.hget(key, "descricao");
                    System.out.printf("Tarefa %s: %s%n", id, descricao);
                } else {
                    System.out.println("Chave não é um hash: " + key);
                }
            }
        }
    }
    

    public void marcarComoConcluida(String id) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("tarefa:" + id, "status", "concluida");
        }
    }

    public void removerTarefasConcluidas() {
        try (Jedis jedis = jedisPool.getResource()) {
            for (String chave : jedis.keys("tarefa:*")) {
                Map<String, String> tarefa = jedis.hgetAll(chave);
                String status = tarefa.get("status");
                if ("concluida".equals(status)) {
                    jedis.del(chave);
                }
            }
        }
    }

    public static void main(String[] args) {
        GerenciadorDeTarefas gerenciador = new GerenciadorDeTarefas();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Adicionar Tarefa");
            System.out.println("2. Listar Tarefas");
            System.out.println("3. Marcar Tarefa como Concluída");
            System.out.println("4. Remover Tarefas Concluídas");
            System.out.println("5. Sair");

            System.out.print("\nEscolha uma opção: ");
            String escolha = scanner.nextLine();

            switch (escolha) {
                case "1":
                    System.out.print("Digite a descrição da tarefa: ");
                    String descricao = scanner.nextLine();
                    String idTarefa = gerenciador.adicionarTarefa(descricao);
                    System.out.printf("Tarefa adicionada com sucesso! ID: %s%n", idTarefa);
                    break;
                case "2":
                    gerenciador.listarTarefas();
                    break;
                case "3":
                    System.out.print("Digite o ID da tarefa a ser marcada como concluída: ");
                    String idTarefaConcluida = scanner.nextLine();
                    gerenciador.marcarComoConcluida(idTarefaConcluida);
                    System.out.println("Tarefa marcada como concluída!");
                    break;
                case "4":
                    gerenciador.removerTarefasConcluidas();
                    System.out.println("Tarefas concluídas removidas!");
                    break;
                case "5":
                    System.out.println("Saindo do programa...");
                    return;
                default:
                    System.out.println("Opção inválida! Por favor, escolha uma opção válida.");
            }
        }
    }
}
