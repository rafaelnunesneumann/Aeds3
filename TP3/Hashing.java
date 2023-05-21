public class Hashing {

    private int p;
    private Bucket[] buckets;

    //Cria os Buckets e seta o P para 1
    public Hashing() {
        p = 1;
        int numBuckets = (int) Math.pow(2, 15);
        buckets = new Bucket[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            buckets[i] = new Bucket();
            buckets[i].setBucketPointer(i);
        }
    }

    //Metodo de inserir
    public void inserir(int chave, long pos) {
        int indexBucket = hash(chave);
        if (!buckets[buckets[indexBucket].getBucketPointer()].isCheio()) {
            buckets[buckets[indexBucket].getBucketPointer()].set(chave, pos, buckets[buckets[indexBucket].getBucketPointer()].getIndexChave() + 1);
            buckets[buckets[indexBucket].getBucketPointer()].setIndexChave(buckets[buckets[indexBucket].getBucketPointer()].getIndexChave() + 1);
            //System.out.println("Inserido no bucket " + buckets[indexBucket].getBucketPointer() + " Tamanho: " + buckets[indexBucket].getIndexChave());
        } else {
            if (buckets[buckets[indexBucket].getBucketPointer()].getP() < p) {
                int pointer = buckets[indexBucket].getBucketPointer();
                buckets[buckets[indexBucket].getBucketPointer()].setP(p);
                buckets[indexBucket].setP(p);
                buckets[indexBucket].setBucketPointer(indexBucket);
                //System.out.println("Necessario re-inserir");
                reInsert(pointer);
                //System.out.println("Re-inserido com sucesso!");
                inserir(chave, pos);
            } else {
                p++;
                int half = (int) Math.pow(2, p)/2;
                for (int i = half; i < (int)Math.pow(2, p); i++) {
                    buckets[i].setBucketPointer(i-half);
                    //System.out.println("Bucket " + i + " apontando para " + (i-half));
                }
                //System.out.println("Extendeu! P agora é " + p);
                inserir(chave, pos);
            }
        }
    }

    //Metodo de reinserir para quando ha colisao
    public void reInsert(int pointer) {
        int[] chaves = buckets[pointer].getChaves();
        long[] pos = buckets[pointer].getPos();
        buckets[pointer] = new Bucket();
        buckets[pointer].setP(p);
        buckets[pointer].setBucketPointer(pointer);
        buckets[pointer].setIndexChave(0);
        for (int i = 0; i < chaves.length; i++) {
            inserir(chaves[i], pos[i]);
        }
    }

    public long getPos(int chave) {
        long resp = 0;
        int index = hash(chave);
        int[] chaves = buckets[buckets[index].getBucketPointer()].getChaves();
        for (int i = 0; i < chaves.length; i++) {
            if (chaves[i] == chave) {
                resp = buckets[buckets[index].getBucketPointer()].getPos(i);
                i = chaves.length;
            }
        }
        if (resp == 0) {
            for (int i = (int)(Math.pow(2,p))-1; i > 0; i--) {
                chaves = buckets[buckets[i].getBucketPointer()].getChaves();
                for (int j = 0; j < chaves.length; j++) {
                    if (chaves[j] == chave) {
                        resp = buckets[buckets[i].getBucketPointer()].getPos(j);
                        j = chaves.length;
                        i = 0;
                    }
                }
            }
        }
        return resp;
    }

    public Bucket getBucket(int chave) {
        Bucket resp = null;
        int index = hash(chave);
        int[] chaves = buckets[buckets[index].getBucketPointer()].getChaves();
        for (int i = 0; i < chaves.length; i++) {
            if (chaves[i] == chave) {
                resp = buckets[buckets[index].getBucketPointer()];
                i = chaves.length;
            }
        }
        if (resp == null) {
            for (int i = (int)(Math.pow(2,p))-1; i > 0; i--) {
                chaves = buckets[buckets[i].getBucketPointer()].getChaves();
                for (int j = 0; j < chaves.length; j++) {
                    if (chaves[j] == chave) {
                        resp = buckets[buckets[i].getBucketPointer()];
                        j = chaves.length;
                        i = 0;
                    }
                }
            }
        }
        return resp;
    }

    public void atualizar(int chave, long pos) {
        Bucket bucket = getBucket(chave);
        for (int i = 0; i < bucket.getChaves().length; i++) {
            if (bucket.getChave(i) == chave) {
                bucket.set(chave, pos, i);
                i = bucket.getChaves().length;
            }
        }
    }

    public void remover(int chave) {
        Bucket bucket = getBucket(chave);
        for (int i = 0; i < bucket.getChaves().length; i++) {
            if (bucket.getChave(i) == chave) {
                for (int j = i; j < bucket.getIndexChave(); j++) {
                    if (j != bucket.getIndexChave()) {
                        bucket.set(bucket.getChave(j+1), bucket.getPos(j+1), j);
                    }
                }
                bucket.setIndexChave(bucket.getIndexChave()-1);
                i = bucket.getChaves().length;
            }
        }
    }

    public int hash(int chave) {
        return (chave%(int)Math.pow(2, p));
    }
    
}
