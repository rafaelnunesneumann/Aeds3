public class Bucket {

    private int[] chaves;
    private long[] pos;
    private int p;

    private int indexChave;
    private int bucketPointer;

    public Bucket() {
        chaves = new int[1354];
        pos = new long[1354];
        p = 1;
        indexChave = 0;
        bucketPointer = 0;
    }

    public boolean isCheio() {
        if (chaves[1353] != 0) {
            return true;
        }
        return false;
    }

    public void set(int chave, long pos, int index) {
        chaves[index] = chave;
        this.pos[index] = pos;
    }

    public int getChave(int index) {
        return chaves[index];
    }

    public long getPos(int index) {
        return pos[index];
    }

    public long[] getPos() {
        return pos;
    }

    public void setChave(int chave, int index) {
        chaves[index] = chave;
    }

    public void setPos(long pos, int index) {
        this.pos[index] = pos;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getP() {
        return p;
    }

    public void setIndexChave(int indexChave) {
        this.indexChave = indexChave;
    }

    public int getIndexChave() {
        return indexChave;
    }

    public void setBucketPointer(int bucketPointer) {
        this.bucketPointer = bucketPointer;
    }

    public int getBucketPointer() {
        return bucketPointer;
    }

    public int[] getChaves() {
        return chaves;
    }
    
}
