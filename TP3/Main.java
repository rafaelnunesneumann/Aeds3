import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

class Main {

    public static void main(String[] args) throws Exception {
        // Muda o charset para UTF-8
        MyIO.setCharset("UTF-8");
        int sair = 0;
        ArvoreB arvoreB = new ArvoreB(8);
        Hashing hash = new Hashing();
        HashMap<String, ArrayList<Integer>> nomes = new HashMap<>();
        HashMap<String, ArrayList<Integer>> datas = new HashMap<>();

        RandomAccessFile arq = new RandomAccessFile("games.db", "rw");
        // Pega os arquivos da base de dados (Se existir) e cria a arvore B, hashing e
        // listas invertidas
        if (arq.length() > 0) {
            int id = 0;
            int lastID = arq.readInt();
            while (id != lastID) {
                long pos = arq.getFilePointer();
                int flag = arq.readInt();
                if (flag == 1) {
                    Game g_temp = new Game();
                    int len = arq.readInt();
                    byte[] ba = new byte[len];
                    arq.read(ba);
                    g_temp.fromByteArray(ba);
                    arvoreB.inserir(g_temp.getID(), pos);
                    hash.inserir(g_temp.getID(), pos);
                    String[] nameSplit = g_temp.getName().split(" ");
                    String date = dateToString(g_temp.getDate());
                    for (int i = 0; i < nameSplit.length; i++) {
                        if (!nomes.containsKey(nameSplit[i])) {
                            ArrayList<Integer> array = new ArrayList<>();
                            array.add(g_temp.getID());
                            nomes.put(nameSplit[i], array);
                        } else {
                            ArrayList<Integer> array = nomes.get(nameSplit[i]);
                            array.add(g_temp.getID());
                            nomes.put(nameSplit[i], array);
                        }
                    }
                    if (!datas.containsKey(date)) {
                        ArrayList<Integer> array = new ArrayList<>();
                        array.add(g_temp.getID());
                        datas.put(date, array);
                    } else {
                        ArrayList<Integer> array = datas.get(date);
                        array.add(g_temp.getID());
                        datas.put(date, array);
                    }
                    id = g_temp.getID();
                } else if (flag == 2) {
                    int len = arq.readInt();
                    arq.seek(arq.getFilePointer() + len);
                }
            }
        }
        arq.close();

        Scanner sc = new Scanner(System.in);
        while (sair == 0) {
            // Menu
            System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            System.out.println("| O que deseja?                                  |");
            System.out.println("|                                                |");
            System.out.println("| 1 - Carregar dados                             |");
            System.out.println("| 2 - Ler um registro                            |");
            System.out.println("| 3 - Atualizar um registro                      |");
            System.out.println("| 4 - Deletar um registro                        |");
            System.out.println("| 5 - Ordenaçao externa                          |");
            System.out.println("| 6 - Compressao                                 |");
            System.out.println("| 7 - Descompressao                              |");
            System.out.println("| 8 - Sair                                       |");
            System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            int escolha = sc.nextInt();
            // Switch para a escolha do usuario
            switch (escolha) {
                // Create
                case 1:
                    // Passagem de arquivos do csv para a base de dados
                    lerArquivoCsv(arvoreB, hash, nomes, datas);
                    System.out.println("Arquivos carregados!");
                    break;
                // Read
                case 2:
                    int voltar = 0;
                    int op;
                    while (voltar == 0) {
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        System.out.println("| Busca                                           |");
                        System.out.println("|                                                 |");
                        System.out.println("| 1 - Busca Sequencial                            |");
                        System.out.println("| 2 - Busca por indice com Arvore B               |");
                        System.out.println("| 3 - Busca por indice com Hashing estendido      |");
                        System.out.println("| 4 - Busca por lista invertida                   |");
                        System.out.println("| 5 - Voltar                                      |");
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        op = MyIO.readInt();
                        switch (op) {
                            case 1:
                                arq = new RandomAccessFile("games.db", "rw");
                                // Verifica se o arquivo nao esta vazio
                                if (arq.length() == 0) {
                                    System.out.println("Carregue a base de dados primeiro!");
                                    break;
                                }
                                System.out.println("Qual o id do registro a ser procurado?");
                                int id = MyIO.readInt();
                                Game g_temp = new Game();
                                byte[] ba;
                                int len;
                                boolean achou = false;

                                // Le o ultimo id cadastrado no cabeçalho
                                int lastID = arq.readInt();
                                long pos = 0;

                                // Le o arquivo sequencialmente até o ID ser igual ao ultimo ID
                                while (g_temp.getID() != lastID && achou == false) {
                                    pos = arq.getFilePointer();
                                    // Leitura do flag para saber se o registro é valido
                                    int flag = arq.readInt();
                                    if (flag == 1) {
                                        // Caso o registro seja valido
                                        // Leitura do tamanho do registro
                                        len = arq.readInt();
                                        // Criaçao e leitura do array de bytes de acordo com o tamanho do registro
                                        ba = new byte[len];
                                        arq.read(ba);
                                        // Movendo os dados do vetor de bytes para o objeto
                                        g_temp.fromByteArray(ba);
                                        // Caso achar o id, parar a busca e salvar os dados
                                        if (g_temp.getID() == id) {
                                            achou = true;
                                        }
                                    } else if (flag == 2) {
                                        // Caso o registro nao seja valido
                                        // Leitura do tamanho do registro
                                        len = arq.readInt();
                                        // Pular registro
                                        arq.seek(arq.getFilePointer() + len);
                                    }
                                }
                                arq.close();
                                // Impressao dos resultados na tela
                                if (achou == true) {
                                    System.out.println(g_temp);
                                    System.out.println("Posiçao: " + pos);
                                } else {
                                    System.out.println("ID nao encontrado!");
                                }
                                System.out.println("Numero de registros: " + contarRegistros("games.db", true));
                                voltar = 1;
                                break;
                            case 2:
                                arq = new RandomAccessFile("games.db", "rw");
                                // Verifica se o arquivo nao esta vazio
                                if (arq.length() == 0) {
                                    System.out.println("Carregue a base de dados primeiro!");
                                    break;
                                }
                                System.out.println("Qual o id do registro a ser procurado?");
                                id = MyIO.readInt();
                                pos = arvoreB.buscarPos(id);
                                g_temp = new Game();
                                if (pos != 0) {
                                    arq.seek(pos);
                                    arq.readInt();
                                    len = arq.readInt();
                                    ba = new byte[len];
                                    arq.read(ba);
                                    g_temp.fromByteArray(ba);
                                    System.out.println(g_temp);
                                    System.out.println("Posiçao: " + pos);
                                    System.out.println("Numero de registros: " + contarRegistros("games.db", true));
                                } else {
                                    System.out.println("ID nao encontrado!");
                                }
                                arq.close();
                                voltar = 1;
                                break;
                            case 3:
                                arq = new RandomAccessFile("games.db", "rw");
                                // Verifica se o arquivo nao esta vazio
                                if (arq.length() == 0) {
                                    System.out.println("Carregue a base de dados primeiro!");
                                    break;
                                }
                                System.out.println("Qual o id do registro a ser procurado?");
                                id = MyIO.readInt();
                                pos = hash.getPos(id);
                                g_temp = new Game();
                                if (pos != 0) {
                                    arq.seek(pos);
                                    arq.readInt();
                                    len = arq.readInt();
                                    ba = new byte[len];
                                    arq.read(ba);
                                    g_temp.fromByteArray(ba);
                                    System.out.println(g_temp);
                                    System.out.println("Posiçao: " + pos);
                                    System.out.println("Numero de registros: " + contarRegistros("games.db", true));
                                } else {
                                    System.out.println("ID nao encontrado!");
                                }
                                arq.close();
                                voltar = 1;
                                break;
                            case 4:
                                arq = new RandomAccessFile("games.db", "rw");
                                // Verifica se o arquivo nao esta vazio
                                if (arq.length() == 0) {
                                    System.out.println("Carregue a base de dados primeiro!");
                                    break;
                                }
                                System.out.println("Digite os termos a serem procurados separados por ' '.");
                                String resp = MyIO.readLine();
                                String[] separado = resp.split(" ");
                                for (int i = 0; i < separado.length; i++) {
                                    if (nomes.containsKey(separado[i]) || datas.containsKey(separado[i])) {
                                        System.out.println("Jogos que contem: '" + separado[i] + "':");
                                        System.out.println(" ");
                                        if (isFormat(separado[i])) {
                                            ArrayList<Integer> conjunto = datas.get(separado[i]);
                                            for (int j = 0; j < conjunto.size(); j++) {
                                                System.out.println((j + 1) + " - ID: " + conjunto.get(j));
                                            }
                                            System.out.println(" ");
                                        } else {
                                            ArrayList<Integer> conjunto = nomes.get(separado[i]);
                                            for (int j = 0; j < conjunto.size(); j++) {
                                                System.out.println((j + 1) + " - ID: " + conjunto.get(j));
                                            }
                                            System.out.println(" ");
                                        }
                                    } else {
                                        System.out.println("Nao foi encontrado resultado para: '" + separado[i] + "'");
                                    }
                                }
                                arq.close();
                                voltar = 1;
                                break;
                            case 5:
                                voltar = 1;
                                break;
                        }
                    }
                    break;
                // Update
                case 3:
                    arq = new RandomAccessFile("games.db", "rw");
                    RandomAccessFile indiceArvoreB = new RandomAccessFile("indiceArvoreB.db", "rw");
                    RandomAccessFile indiceHashing = new RandomAccessFile("indiceHashing.db", "rw");
                    // Verifica se o arquivo nao esta vazio
                    if (arq.length() == 0) {
                        System.out.println("Carregue a base de dados primeiro!");
                        break;
                    }
                    System.out.println("Digite o id em que deseja atualizar:");
                    int id = MyIO.readInt();
                    long pos = 0;
                    Game g_temp = new Game();
                    int len = 0;
                    boolean achou = false;

                    // Le o ultimo id cadastrado no cabeçalho
                    int lastID = arq.readInt();

                    // Busca pela arvore B
                    pos = arvoreB.buscarPos(id);

                    if (pos != 0) {
                        arq.seek(pos);
                        arq.readInt();
                        len = arq.readInt();
                        byte[] ba = new byte[len];
                        arq.read(ba);
                        g_temp.fromByteArray(ba);
                        // Declaraçao de variaveis
                        String newName, newDateString;
                        Date newDate;
                        int newLan_support, newRequired_age;
                        String[] newDeveloper, newPublisher, newPlatforms, newCategories, newGenres;

                        // Scanner para a mudança de nome
                        System.out.println("Voce esta editando o jogo " + g_temp.getName() + ", com id: " + id);
                        System.out.println("Digite o novo nome do jogo: ");
                        newName = MyIO.readLine();

                        // Scanner para a mudança de data
                        System.out.println(
                                "Digite a nova data de lançamento do jogo no formato: YYYY-MM-DD, Ex: 2008-11-05");
                        newDateString = MyIO.readLine();
                        // Caso a entrada nao esteja no formato adequado, repete o scanner
                        while (isFormat(newDateString) == false) {
                            System.out.println("Formato inadequado ou data invalida! Insira a data novamente.");
                            newDateString = sc.nextLine();
                        }
                        newDate = toDate(newDateString);

                        // Scanner para a mudança do language support
                        System.out.println("O jogo tem suporte para ingles? (SIM/NAO)");
                        String resp = MyIO.readLine();
                        // Caso a entrada esteja invalida, repete o scanner
                        while (resp.equals("SIM") == false && resp.equals("NAO") == false) {
                            System.out.println("Resposta invalida! Insira a resposta novamente:");
                            resp = MyIO.readLine();
                        }
                        // Seta o language support de acordo com a resposta
                        if (resp.equals("SIM")) {
                            newLan_support = 1;
                        } else {
                            newLan_support = 0;
                        }

                        // Scanner para a mudança de idade necessaria
                        System.out.println("Digite a idade necessaria para jogar o jogo: ");
                        newRequired_age = MyIO.readInt();

                        // Scanner para a mudança de desenvolvedores, utilizando "," como separador
                        System.out.println(
                                "Digite o(s) desenvolvedor(es) do jogo usando separaçao por ','. Ex: Dev1, Dev2, Dev3");
                        newDeveloper = MyIO.readLine().split(",");

                        // Scanner para a mudança de publicadores, utilizando "," como separador
                        System.out.println(
                                "Digite o(s) publicador(es) do jogo usando separaçao por ','. Ex: Pub1, Pub2, Pub3");
                        newPublisher = MyIO.readLine().split(",");

                        // Scanner para a mudança de plataformas, utilizando "," como separador
                        System.out.println(
                                "Digite a(s) plataforma(s) do jogo usando separaçao por ','. Ex: windows, mac, linux");
                        newPlatforms = MyIO.readLine().split(",");

                        // Scanner para a mudança de categorias, utilizando "," como separador
                        System.out.println(
                                "Digite a(s) categoria(s) do jogo usando separaçao por ','. Ex: Online, Multiplayer local, Valve Anticheat enabled");
                        newCategories = MyIO.readLine().split(",");

                        // Scanner para a mudança de generos, utilizando "," como separador
                        System.out.println(
                                "Digite o(s) genero(s) do jogo usando separaçao por ','. Ex: Action, Adventure, Indie");
                        newGenres = MyIO.readLine().split(",");

                        // Criaçao de um novo game com os dados recebidos
                        Game newGame = new Game(id, newLan_support, newRequired_age, newName, newDate, newDeveloper,
                                newPublisher, newPlatforms, newCategories, newGenres);
                        byte[] newBa;
                        int newLen;

                        // Transformaçao do novo jogo em um array de bytes
                        newBa = newGame.toByteArray();
                        // Pegando o tamanho desse novo array
                        newLen = newBa.length;
                        // Verificaçao de tamanho dos registros
                        if (newLen == len) {
                            // Caso o registro seja igual ao antigo, setar no mesmo local, sobrepondo os
                            // dados
                            arq.seek(pos);
                            arq.writeInt(1);
                            arq.writeInt(len); // Tamanho do registro em bytes
                            arq.write(newBa);
                        } else {
                            // Caso o registro seja maior ou menor que o antigo, mover para o final do
                            // arquivo e "apagar" o outro
                            arq.seek(pos);
                            arq.writeInt(2); // Lapide registrando como invalido

                            arq.seek(arq.length()); // Move para o final do arquivo
                            // Criaçao de um novo registro no final do arquivo
                            pos = arq.getFilePointer();
                            arq.writeInt(1);
                            arq.writeInt(newLen);
                            arq.write(newBa);
                            arvoreB.atualizar(id, pos); // Atualiza na arvore
                            hash.atualizar(id, pos); // Atualiza no hash
                            // Atualizar nos arquivos invertidos
                            String[] separado = g_temp.getName().split(" ");
                            String date = dateToString(g_temp.getDate());
                            for (int i = 0; i < separado.length; i++) {
                                ArrayList<Integer> array = nomes.get(separado[i]);
                                nomes.remove(separado[i], array);
                                for (int j = 0; j < array.size(); j++) {
                                    if (array.get(j) == g_temp.getID()) {
                                        array.remove(j);
                                        j = array.size();
                                    }
                                }
                                nomes.put(separado[i], array);
                            }
                            ArrayList<Integer> array = datas.get(date);
                            datas.remove(date, g_temp.getID());
                            for (int i = 0; i < array.size(); i++) {
                                if (array.get(i) == g_temp.getID()) {
                                    array.remove(i);
                                    i = array.size();
                                }
                            }
                            datas.put(date, array);
                            separado = newName.split(" ");
                            date = dateToString(newGame.getDate());
                            for (int i = 0; i < separado.length; i++) {
                                if (nomes.containsKey(separado[i])) {
                                    ArrayList<Integer> arrayNome = nomes.get(separado[i]);
                                    arrayNome.add(id);
                                    nomes.put(separado[i], arrayNome);
                                } else {
                                    ArrayList<Integer> arrayNome = new ArrayList<>();
                                    arrayNome.add(id);
                                    nomes.put(separado[i], arrayNome);
                                }
                            }
                            if (datas.containsKey(date)) {
                                ArrayList<Integer> arrayData = datas.get(date);
                                arrayData.add(id);
                                datas.put(date, arrayData);
                            } else {
                                ArrayList<Integer> arrayData = new ArrayList<>();
                                arrayData.add(id);
                                datas.put(date, arrayData);
                            }
                            achou = false;
                            // Muda a posicão no arquivo de indices da arvore B e hashing
                            while (achou = false) {
                                int chave = indiceArvoreB.readInt();
                                long posicao = indiceArvoreB.getFilePointer();
                                indiceArvoreB.readLong();
                                if (chave == id) {
                                    indiceArvoreB.seek(posicao);
                                    indiceArvoreB.writeLong(pos);
                                    indiceHashing.seek(posicao);
                                    indiceHashing.writeLong(pos);
                                    achou = true;
                                }
                            }
                            // Mudar o ultimo id cadastrado no cabeçalho
                            arq.seek(0);
                            arq.writeInt(id);
                        }
                        // Impressao dos resultados na tela
                        System.out.println("Arquivo atualizado com sucesso!");
                    } else {
                        System.out.println("ID nao encontrado!");
                    }
                    arq.close();
                    indiceArvoreB.close();
                    break;
                // Delete
                case 4:
                    arq = new RandomAccessFile("games.db", "rw");
                    g_temp = new Game();
                    lastID = arq.readInt();
                    int newLastID = 0;
                    achou = false;
                    pos = 0;

                    // Verifica se o arquivo nao esta vazio
                    if (arq.length() == 0) {
                        System.out.println("Carregue a base de dados primeiro!");
                        break;
                    }
                    System.out.println("Digite o id do registro a deletar:");
                    id = MyIO.readInt();

                    // Busca sequencial pelo arquivo
                    while (g_temp.getID() != lastID && achou == false) {
                        if (g_temp.getID() != lastID) {
                            newLastID = g_temp.getID(); // Salva o penultimo id
                        }
                        pos = arq.getFilePointer(); // Salva a posiçao do registro

                        int flag = arq.readInt();
                        if (flag == 1) {
                            len = arq.readInt();
                            byte[] ba = new byte[len];
                            arq.read(ba);
                            g_temp.fromByteArray(ba);
                            if (g_temp.getID() == id) {
                                achou = true;
                            }
                        } else if (flag == 2) {
                            len = arq.readInt();
                            arq.seek(arq.getFilePointer() + len);
                        }
                    }

                    // Caso encontre o ID
                    if (achou == true) {
                        arq.seek(pos); // Vai ate a posiçao do registro
                        arq.writeInt(2); // Lapide registrando como invalido
                        arvoreB.remover(id); // Remove da arvore B
                        hash.remover(id); // Remove do hash
                        // Remover das listas invertidas
                        String[] separado = g_temp.getName().split(" ");
                        for (int i = 0; i < separado.length; i++) {
                            ArrayList<Integer> array = nomes.get(separado[i]);
                            nomes.remove(separado[i], g_temp.getID());
                            for (int j = 0; j < array.size(); j++) {
                                if (array.get(j) == id) {
                                    array.remove(j);
                                    j = array.size();
                                }
                            }
                            nomes.put(separado[i], array);
                        }
                        String date = dateToString(g_temp.getDate());
                        ArrayList<Integer> array = datas.get(date);
                        datas.remove(date, array);
                        for (int i = 0; i < array.size(); i++) {
                            if (array.get(i) == id) {
                                array.remove(i);
                                i = array.size();
                            }
                        }
                        datas.put(date, array);
                        System.out.println(g_temp.getName() + " deletado com sucesso!");
                        // Caso o registro deletado fosse o ultimo, mudar o cabeçalho para o penultimo
                        // ID (newLastID)
                        if (id == lastID) {
                            arq.seek(0); // Vai ate o começo do arquivo
                            arq.writeInt(newLastID); // Salva o ultimo ID valido
                        }
                    } else {
                        System.out.println("ID nao encontrado!");
                    }

                    arq.close();
                    break;
                // Sair
                case 5:
                    voltar = 0;
                    while (voltar == 0) {
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        System.out.println("|                Ordenaçao Externa                |");
                        System.out.println("|                                                 |");
                        System.out.println("| 1 - Intercalaçao comum                          |");
                        System.out.println("| 2 - Intercalaçao com blocos de tamanho variavel |");
                        System.out.println("| 3 - Intercalaçao com seleçao por substituiçao   |");
                        System.out.println("| 4 - Voltar                                      |");
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        op = MyIO.readInt();
                        switch (op) {
                            case 1:
                                interBalanceada();
                                voltar = 1;
                                break;
                            case 2:
                                voltar = 1;
                                break;
                            case 3:
                                voltar = 1;
                                break;
                            case 4:
                                voltar = 1;
                                break;
                        }
                    }
                    break;
                case 6:
                    voltar = 0;
                    while (voltar == 0) {
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        System.out.println("|              Compressao de Dados                |");
                        System.out.println("|                                                 |");
                        System.out.println("| 1 - Compressao por LZW                          |");
                        System.out.println("| 2 - Compressao por Huffman                      |");
                        System.out.println("| 3 - Voltar                                      |");
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        op = MyIO.readInt();
                        switch (op) {
                            case 1:
                                LZW lzw = new LZW();
                                lzw.comprimir("games.db", "gamesLZWCompressao.db");
                                System.out.println("Comprimido com sucesso!");
                                voltar = 1;
                                break;
                            case 2:
                                voltar = 1;
                                break;
                            case 3:
                                voltar = 1;
                                break;
                        }
                    }
                    break;
                case 7:
                    voltar = 0;
                    while (voltar == 0) {
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        System.out.println("|              Descompressao de Dados             |");
                        System.out.println("|                                                 |");
                        System.out.println("| 1 - Descompressao por LZW                       |");
                        System.out.println("| 2 - Descompressao por Huffman                   |");
                        System.out.println("| 3 - Voltar                                      |");
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        op = MyIO.readInt();
                        switch (op) {
                            case 1:
                                LZW lzw = new LZW();
                                lzw.descomprimir("gamesLZWCompressao.db", "gamesLZWDescompressao.db");
                                System.out.println("Descomprimido com sucesso!");
                                voltar = 1;
                                break;
                            case 2:
                                voltar = 1;
                                break;
                            case 3:
                                voltar = 1;
                                break;
                        }
                    }
                    break;
                case 8:
                    sair = 1;
                    break;
            }
        }
        sc.close();
    }

    // Metodo para passar os dados do arquivo CSV para a base de dados
    public static void lerArquivoCsv(ArvoreB arvore, Hashing hash, HashMap<String, ArrayList<Integer>> nomes, HashMap<String, ArrayList<Integer>> datas) throws Exception {
        // Criaçao de um vetor de games
        Game[] game = new Game[30000];
        // Abertura do scanner para ler o csv
        Scanner sc = new Scanner(new File("steam.csv"));
        sc.useDelimiter(","); // Delimitador de ","

        // Variavel para a contagem de games
        int i = 0;
        while (sc.hasNextLine()) {
            String s = sc.nextLine(); // Leitura de toda a linha

            // Separa as linhas por "," fora do ""
            String[] separado = s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

            // Leitura dos atributos para cada variavel, utilizando a separaçao por ","

            int id = Integer.valueOf(separado[0]);

            String name = separado[1];

            String data = separado[2];
            Date date = toDate(data);

            int lan_support = Integer.valueOf(separado[3]);

            String developer[] = new String[separado[4].split(";").length];
            for (int j = 0; j < separado[4].split(";").length; j++) {
                developer[j] = separado[4].split(";")[j];
            }

            String publisher[] = new String[separado[5].split(";").length];
            for (int j = 0; j < separado[5].split(";").length; j++) {
                publisher[j] = separado[5].split(";")[j];
            }

            String platforms[] = new String[separado[6].split(";").length];
            for (int j = 0; j < separado[6].split(";").length; j++) {
                platforms[j] = separado[6].split(";")[j];
            }

            int required_age = Integer.valueOf(separado[7]);
            String categories[] = new String[separado[8].split(";").length];
            for (int j = 0; j < separado[8].split(";").length; j++) {
                categories[j] = separado[8].split(";")[j];
            }

            String genres[] = new String[separado[9].split(";").length];
            for (int j = 0; j < separado[9].split(";").length; j++) {
                genres[j] = separado[9].split(";")[j];
            }

            // Cria o game com os atributos recebidos
            game[i] = new Game(id, lan_support, required_age, name, date, developer, publisher, platforms, categories,
                    genres);
            i++;
        }
        sc.close();

        // Salvar todos os games no arquivo em bytes

        byte[] ba;
        long[] pos = new long[i];

        // Salva todos os games na base de dados
        try {
            RandomAccessFile arq = new RandomAccessFile("games.db", "rw");
            RandomAccessFile indiceArvoreB = new RandomAccessFile("indiceArvoreB.db", "rw");
            RandomAccessFile indiceHashing = new RandomAccessFile("indiceHashing.db", "rw");
            arq.writeInt(game[i - 1].getID()); // Salva o ultimo ID cadastrado no cabeçalho
            for (int j = 0; j < i; j++) {
                ba = game[j].toByteArray();
                pos[j] = arq.getFilePointer();
                arq.writeInt(1);
                arq.writeInt(ba.length); // Tamanho do registro em bytes
                arq.write(ba);
                arvore.inserir(game[j].getID(), pos[j]);
                hash.inserir(game[j].getID(), pos[j]);
                indiceArvoreB.writeInt(game[j].getID());
                indiceArvoreB.writeLong(pos[j]);
                indiceHashing.writeInt(game[j].getID());
                indiceHashing.writeLong(pos[j]);
                String[] nameSplit = game[j].getName().split(" ");
                String date = dateToString(game[j].getDate());
                for (int k = 0; k < nameSplit.length; k++) {
                    if (!nomes.containsKey(nameSplit[k])) {
                        ArrayList<Integer> array = new ArrayList<>();
                        array.add(game[j].getID());
                        nomes.put(nameSplit[k], array);
                    } else {
                        ArrayList<Integer> array = nomes.get(nameSplit[k]);
                        array.add(game[j].getID());
                        nomes.put(nameSplit[k], array);
                    }
                }
                if (!datas.containsKey(date)) {
                    ArrayList<Integer> array = new ArrayList<>();
                    array.add(game[j].getID());
                    datas.put(date, array);
                } else {
                    ArrayList<Integer> array = datas.get(date);
                    array.add(game[j].getID());
                    datas.put(date, array);
                }
            }
            arq.close();
            indiceArvoreB.close();
            indiceHashing.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Metodo para converter uma String formatada em Data
    public static Date toDate(String data) throws Exception {
        Date date;
        date = new SimpleDateFormat("yyyy-MM-dd").parse(data);
        return date;
    }

    // Metodo para converter uma Data em String formatada
    public static String dateToString(Date date) throws Exception {
        String s = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return s;
    }

    // Metodo para verificar o formato de uma String de tamanho fixo
    public static boolean isFormat(String date) {
        String[] separado = date.split("-"); // Separaçao da Data por "-"
        if (separado.length == 3) {
            try {
                Integer.parseInt(separado[0]); // Verifica se é inteiro, caso contrario, entra no catch e retorna false
                int ano = Integer.valueOf(separado[0]);
                if (ano >= 1000 && ano <= 2023) { // Verifica se o ano é valido
                    try {
                        Integer.parseInt(separado[1]); // Verifica se é inteiro, caso contrario, entra no catch e
                                                       // retorna false
                        int mes = Integer.valueOf(separado[1]);
                        if (mes >= 1 && mes <= 12) { // Verifica se o mes é valido
                            try {
                                Integer.parseInt(separado[2]); // Verifica se é inteiro, caso contrario, entra no catch
                                                               // e retorna false
                                int dia = Integer.valueOf(separado[2]);
                                if (dia >= 1 && dia <= 31) { // Verifica se o dia é valido
                                    // Retorna verdadeiro caso esteja no formato
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public static void interBalanceada() throws Exception {
        // Metodo para pegar e ordenar em memoria principal de 5 em 5
        String arquivoEntrada = "games.db";
        String arquivoTemp1 = "temp1.db";
        String arquivoTemp2 = "temp2.db";

        int tamanhoBloco = 5;
        int n = contarRegistros(arquivoEntrada, true);

        // Cria os leitores e escritores
        RandomAccessFile leitorEntrada = new RandomAccessFile(arquivoEntrada, "rw");
        RandomAccessFile escritorTemp1 = new RandomAccessFile(arquivoTemp1, "rw");
        RandomAccessFile escritorTemp2 = new RandomAccessFile(arquivoTemp2, "rw");

        // Lê e escreve os primeiros blocos
        int numBlocos = (int) Math.ceil((double) n / tamanhoBloco);
        int[] tamanhos = new int[numBlocos];
        leitorEntrada.readInt();
        int len;
        byte[] ba;
        for (int i = 0; i < numBlocos; i++) {
            tamanhos[i] = i == numBlocos - 1 ? n % tamanhoBloco : tamanhoBloco;
            if (tamanhos[i] == 0) {
                tamanhos[i] = tamanhoBloco;
            }
            Game[] bloco = new Game[tamanhos[i]];
            for (int j = 0; j < tamanhos[i]; j++) {
                int flag = leitorEntrada.readInt();
                if (flag == 1) {
                    Game gameToArray = new Game();
                    len = leitorEntrada.readInt();
                    ba = new byte[len];
                    leitorEntrada.read(ba);
                    gameToArray.fromByteArray(ba);
                    bloco[j] = gameToArray;
                } else if (flag == 2) {
                    len = leitorEntrada.readInt();
                    leitorEntrada.seek(leitorEntrada.getFilePointer() + len);
                    j -= 1;
                }
            }
            ordenar(0, bloco.length - 1, bloco);
            for (int j = 0; j < tamanhos[i]; j++) {
                if (i % 2 == 0) {
                    ba = bloco[j].toByteArray();
                    len = ba.length;
                    escritorTemp1.writeInt(len);
                    escritorTemp1.write(ba);
                } else {
                    ba = bloco[j].toByteArray();
                    len = ba.length;
                    escritorTemp2.writeInt(len);
                    escritorTemp2.write(ba);
                }
            }
        }
        leitorEntrada.close();
        escritorTemp1.close();
        escritorTemp2.close();

        // Intercala os blocos
        int temp = intercala(arquivoEntrada, 12);

        new File(arquivoEntrada).delete();

        // Salva os registros no arquivo principal, colocando o ultimo id no cabeçalho
        int numeroRegistros = contarRegistros(temp == 1 ? "temp3.db" : "temp1.db", false);
        int ultimoID = findLastID(temp == 1 ? "temp3.db" : "temp1.db");

        RandomAccessFile escritorEntrada = new RandomAccessFile(arquivoEntrada, "rw");
        RandomAccessFile leitorTemp3 = new RandomAccessFile(temp == 1 ? "temp3.db" : "temp1.db", "rw");
        escritorEntrada.writeInt(ultimoID);

        for (int i = 0; i < numeroRegistros; i++) {
            int tamanho = leitorTemp3.readInt();
            byte[] by = new byte[tamanho];
            leitorTemp3.read(by);

            escritorEntrada.writeInt(1);
            escritorEntrada.writeInt(tamanho);
            escritorEntrada.write(by);
        }

        escritorEntrada.close();
        leitorTemp3.close();
        new File(temp == 1 ? "temp3.db" : "temp1.db").delete();
        new File(temp == 1 ? "temp4.db" : "temp2.db").delete();

        System.out.println("Ordenação concluida com sucesso!");
    }

    public static int intercala(String arquivoEntrada, int numIntercala) throws Exception {
        // Metodo para fazer as intercalações
        int temp = 0;
        // Repete até o numero de intercalações
        for (int i = 0; i < numIntercala; i++) {
            if (i % 2 == 0) {
                temp = 1;
            } else {
                temp = 2;
            }
            // Potencia para saber o numero de divisoes
            int pow = (int) Math.pow(2, (numIntercala - i) - 1);
            // Contagem dos registros
            int n = contarRegistros(temp == 1 ? "temp1.db" : "temp3.db", false);
            int n2 = contarRegistros(temp == 1 ? "temp2.db" : "temp4.db", false);

            // Calculo do tamanho do bloco
            int tamanhoBloco = n / pow;
            int tamanhoBloco2 = n2 / pow;

            int cont1 = n;
            int cont2 = n2;

            // Calculo do numeros de blocos
            int numBlocos = (int) Math.ceil((double) n / tamanhoBloco);
            int numBlocos2 = (int) Math.ceil((double) n2 / tamanhoBloco2);

            int[] tamanhos = new int[numBlocos];
            int[] tamanhos2 = new int[numBlocos2];

            // Criação dos leitores e escritores
            RandomAccessFile leitorTemp1 = new RandomAccessFile(temp == 1 ? "temp1.db" : "temp3.db", "rw");
            RandomAccessFile leitorTemp2 = new RandomAccessFile(temp == 1 ? "temp2.db" : "temp4.db", "rw");
            leitorTemp1.seek(0);
            leitorTemp2.seek(0);

            RandomAccessFile escritorTemp3 = new RandomAccessFile(temp == 1 ? "temp3.db" : "temp1.db", "rw");
            RandomAccessFile escritorTemp4 = new RandomAccessFile(temp == 1 ? "temp4.db" : "temp2.db", "rw");
            escritorTemp3.seek(0);
            escritorTemp4.seek(0);

            int arquivo = 1; // indice para saber qual arquivo escrever/ler

            for (int j = 0; j < pow; j++) {
                if ((int) Math.ceil((double) cont1 / pow) == (int) Math.ceil((double) n / pow)) { // Caso o valor seja
                                                                                                  // arredondado
                    tamanhos[j] = (int) Math.ceil((double) n / pow);
                } else {
                    tamanhos[j] = j == numBlocos - 1 ? n % tamanhoBloco : tamanhoBloco;
                    if (tamanhos[j] == 0) {
                        tamanhos[j] = tamanhoBloco;
                    }
                }
                cont1--;
                if ((int) Math.ceil((double) cont2 / pow) == (int) Math.ceil((double) n2 / pow)) {
                    tamanhos2[j] = (int) Math.ceil((double) n2 / pow);
                } else {
                    tamanhos2[j] = j == numBlocos2 - 1 ? n2 % tamanhoBloco2 : tamanhoBloco2;
                    if (tamanhos2[j] == 0) {
                        tamanhos2[j] = tamanhoBloco2;
                    }
                }
                cont2--;
                Game[] array1 = new Game[tamanhos[j]];
                Game[] array2 = new Game[tamanhos2[j]];

                // Pega dos arquivos temporarios e salva no array
                for (int k = 0; k < tamanhos[j]; k++) {
                    int len = leitorTemp1.readInt();
                    byte[] ba = new byte[len];
                    leitorTemp1.read(ba);
                    Game game = new Game();
                    game.fromByteArray(ba);
                    array1[k] = game;
                }

                // Pega dos arquivos temporarios e salva no array
                for (int k = 0; k < tamanhos2[j]; k++) {
                    int len = leitorTemp2.readInt();
                    byte[] ba = new byte[len];
                    leitorTemp2.read(ba);
                    Game game = new Game();
                    game.fromByteArray(ba);
                    array2[k] = game;
                }

                // Calculo do numero de comparações necessarias
                int numComp = tamanhos[j] + tamanhos2[j];

                int indice1 = 0;
                int indice2 = 0;
                int indice3 = 1;
                int indice4 = 1;

                // Intercalação, verificando se o numero é menor e aumentando o indice
                for (int k = 0; k < numComp - 1; k++) {
                    if (indice1 < array1.length) {
                        if (indice2 < array2.length) {
                            Game g1 = array1[indice1];
                            Game g2 = array2[indice2];
                            if (array1[indice1].getID() < array2[indice2].getID()) {
                                if (indice1 != array1.length) {
                                    indice1++;
                                    indice3++;
                                }
                                if (g1.getID() == 10) {
                                }

                                byte[] ba = g1.toByteArray();
                                int len = ba.length;
                                if (arquivo == 1) {
                                    escritorTemp3.writeInt(len);
                                    escritorTemp3.write(ba);
                                } else if (arquivo == 2) {
                                    escritorTemp4.writeInt(len);
                                    escritorTemp4.write(ba);
                                }
                            } else {
                                if (indice2 != array2.length) {
                                    indice2++;
                                    indice4++;
                                }
                                if (g2.getID() == 10) {
                                }
                                byte[] ba = g2.toByteArray();
                                int len = ba.length;
                                if (arquivo == 1) {
                                    escritorTemp3.writeInt(len);
                                    escritorTemp3.write(ba);
                                } else if (arquivo == 2) {
                                    escritorTemp4.writeInt(len);
                                    escritorTemp4.write(ba);
                                }
                            }
                        } else {
                            if (array1[indice1].getID() < array1[indice3].getID()) {
                                if (array1[indice1].getID() == 10) {
                                }
                                byte[] ba = array1[indice1].toByteArray();
                                int len = ba.length;
                                if (arquivo == 1) {
                                    escritorTemp3.writeInt(len);
                                    escritorTemp3.write(ba);
                                } else if (arquivo == 2) {
                                    escritorTemp4.writeInt(len);
                                    escritorTemp4.write(ba);
                                }
                                indice1++;
                                indice3++;
                            } else {
                                if (array1[indice3].getID() == 10) {
                                }
                                byte[] ba = array1[indice3].toByteArray();
                                int len = ba.length;
                                if (arquivo == 1) {
                                    escritorTemp3.writeInt(len);
                                    escritorTemp3.write(ba);
                                } else if (arquivo == 2) {
                                    escritorTemp4.writeInt(len);
                                    escritorTemp4.write(ba);
                                }
                                indice3++;
                            }
                        }
                    } else {
                        if (array2[indice2].getID() < array2[indice4].getID()) {
                            if (array2[indice2].getID() == 10) {
                            }
                            byte[] ba = array2[indice2].toByteArray();
                            int len = ba.length;
                            if (arquivo == 1) {
                                escritorTemp3.writeInt(len);
                                escritorTemp3.write(ba);
                            } else if (arquivo == 2) {
                                escritorTemp4.writeInt(len);
                                escritorTemp4.write(ba);
                            }
                            indice2++;
                            indice4++;
                        } else {
                            if (array2[indice4].getID() == 10) {
                            }
                            byte[] ba = array2[indice4].toByteArray();
                            int len = ba.length;
                            if (arquivo == 1) {
                                escritorTemp3.writeInt(len);
                                escritorTemp3.write(ba);
                            } else if (arquivo == 2) {
                                escritorTemp4.writeInt(len);
                                escritorTemp4.write(ba);
                            }
                            indice4++;
                        }
                    }
                }
                // Calculo para verificar o ultimo numero que sobrou
                if (indice3 == indice1 + 1) {
                    if (indice4 == indice2 + 1) {
                        if (indice1 < array1.length) {
                            byte[] ba = array1[indice1].toByteArray();
                            int len = ba.length;
                            if (arquivo == 1) {
                                escritorTemp3.writeInt(len);
                                escritorTemp3.write(ba);
                            } else if (arquivo == 2) {
                                escritorTemp4.writeInt(len);
                                escritorTemp4.write(ba);
                            }
                        } else if (indice2 < array2.length) {
                            byte[] ba = array2[indice2].toByteArray();
                            int len = ba.length;
                            if (arquivo == 1) {
                                escritorTemp3.writeInt(len);
                                escritorTemp3.write(ba);
                            } else if (arquivo == 2) {
                                escritorTemp4.writeInt(len);
                                escritorTemp4.write(ba);
                            }
                        }
                    } else {
                        if (indice4 >= array2.length) {
                            byte[] ba = array2[indice2].toByteArray();
                            int len = ba.length;
                            if (arquivo == 1) {
                                escritorTemp3.writeInt(len);
                                escritorTemp3.write(ba);
                            } else if (arquivo == 2) {
                                escritorTemp4.writeInt(len);
                                escritorTemp4.write(ba);
                            }
                        } else {
                            byte[] ba = array2[indice4].toByteArray();
                            int len = ba.length;
                            if (arquivo == 1) {
                                escritorTemp3.writeInt(len);
                                escritorTemp3.write(ba);
                            } else if (arquivo == 2) {
                                escritorTemp4.writeInt(len);
                                escritorTemp4.write(ba);
                            }
                        }
                    }
                } else {
                    if (indice3 >= array1.length) {
                        byte[] ba = array1[indice1].toByteArray();
                        int len = ba.length;
                        if (arquivo == 1) {
                            escritorTemp3.writeInt(len);
                            escritorTemp3.write(ba);
                        } else if (arquivo == 2) {
                            escritorTemp4.writeInt(len);
                            escritorTemp4.write(ba);
                        }
                    } else {
                        byte[] ba = array1[indice3].toByteArray();
                        int len = ba.length;
                        if (arquivo == 1) {
                            escritorTemp3.writeInt(len);
                            escritorTemp3.write(ba);
                        } else if (arquivo == 2) {
                            escritorTemp4.writeInt(len);
                            escritorTemp4.write(ba);
                        }
                    }
                }
                if (arquivo == 1) {
                    arquivo = 2;
                } else if (arquivo == 2) {
                    arquivo = 1;
                }
            }
            escritorTemp3.close();
            escritorTemp4.close();
            leitorTemp1.close();
            leitorTemp2.close();

            // Deleta os arquivos
            new File(temp == 1 ? "temp1.db" : "temp3.db").delete();
            new File(temp == 1 ? "temp2.db" : "temp4.db").delete();
        }
        return temp;
    }

    private static int contarRegistros(String nomeArquivo, boolean hasLapide) throws Exception {
        // Metodo para contar a quantidade de registros no arquivo
        int contador = 0;
        try {
            RandomAccessFile arq = new RandomAccessFile(nomeArquivo, "rw");
            Game g_temp = new Game();
            int lastID = arq.readInt();
            int len;
            byte[] ba;
            // Se o arquivo possui lapide
            if (hasLapide == true) {
                while (g_temp.getID() != lastID) {
                    int flag = arq.readInt();
                    if (flag == 1) {
                        len = arq.readInt();
                        ba = new byte[len];
                        arq.read(ba);
                        g_temp.fromByteArray(ba);
                        contador++;
                    } else if (flag == 2) {
                        len = arq.readInt();
                        arq.seek(arq.getFilePointer() + len);
                    }
                }
            } else if (hasLapide == false) {
                arq.seek(0);
                while (arq.getFilePointer() != arq.length()) {
                    len = arq.readInt();
                    ba = new byte[len];
                    arq.read(ba);
                    g_temp.fromByteArray(ba);
                    contador++;
                }
            }
            arq.close();
        } catch (IOException e) {
        }
        return contador;
    }

    private static int findLastID(String nomeArquivo) throws Exception {
        // Metodo para descobrir o ultimo id salvo no arquivo
        int id = 0;
        try {
            RandomAccessFile arq = new RandomAccessFile(nomeArquivo, "rw");
            Game g_temp = new Game();
            int len;
            byte[] ba;
            arq.seek(0);
            while (arq.getFilePointer() != arq.length()) {
                len = arq.readInt();
                ba = new byte[len];
                arq.read(ba);
                g_temp.fromByteArray(ba);
                id = g_temp.getID();
            }
            arq.close();
        } catch (IOException e) {
        }
        return id;
    }

    // Troca de array
    public static void swap(int i, int j, Game[] array) {
        Game temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    // QuickSort
    private static void ordenar(int esq, int dir, Game[] array) {
        if (array.length <= 0) {
            return;
        }
        int i = esq, j = dir;
        Game pivo = array[(dir + esq / 2)];
        while (i <= j) {
            while (array[i].getID() < pivo.getID())
                i++;
            while (array[j].getID() > pivo.getID())
                j--;
            if (i <= j) {
                swap(i, j, array);
                i++;
                j--;
            }
        }
    }
}