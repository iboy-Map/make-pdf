public class PointF {
    public float x;
    public float y;

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF()
    {
    }

    public String ShowPt(){
        return x + ", " + y;
    }

    public float getLat() {
        return x;
    }

    public float getLong() {
        return y;
    }
}
