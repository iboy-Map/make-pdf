public class DrawGeometry {
    private int[] xPoints;
    private int[] yPoints;
    private int nPoints;

    public DrawGeometry(int[] xPoints, int[] yPoints, int nPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.nPoints = nPoints;
    }

    public int[] getxPoints() {
        return xPoints;
    }

    public void setxPoints(int[] xPoints) {
        this.xPoints = xPoints;
    }

    public int[] getyPoints() {
        return yPoints;
    }

    public void setyPoints(int[] yPoints) {
        this.yPoints = yPoints;
    }

    public int getnPoints() {
        return nPoints;
    }

    public void setnPoints(int nPoints) {
        this.nPoints = nPoints;
    }
}
