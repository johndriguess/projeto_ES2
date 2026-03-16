package repo;

import model.Avaliacao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvaliacaoRepository {
    private final File storageFile;
    private Map<String, Avaliacao> avaliacoesById;

    public AvaliacaoRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.storageFile = new File(dataDir, "avaliacoes.db");
        load();
    }

    public AvaliacaoRepository(String filePath) {
        this.storageFile = new File(filePath);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            avaliacoesById = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            avaliacoesById = (Map<String, Avaliacao>) o;
        } catch (Exception e) {
            System.err.println("Não foi possível carregar avaliações. Inicializando vazio. (" + e.getMessage() + ")");
            avaliacoesById = new HashMap<>();
        }
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(avaliacoesById);
        } catch (Exception e) {
            System.err.println("Erro ao salvar avaliações: " + e.getMessage());
        }
    }

    public void save(Avaliacao avaliacao) {
        avaliacoesById.put(avaliacao.getId(), avaliacao);
        persist();
    }

    public List<Avaliacao> findAll() {
        return new ArrayList<>(avaliacoesById.values());
    }
}
