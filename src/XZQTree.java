public class XZQTree {
    private String XZQName;
    private int XZQNum;
    private String LastXZQName;

    public XZQTree(String XZQName, int XZQNum, String lastXZQName) {
        this.XZQName = XZQName;
        this.XZQNum = XZQNum;
        LastXZQName = lastXZQName;
    }

    public String getLastXZQName() {
        return LastXZQName;
    }

    public void setLastXZQName(String lastXZQName) {
        LastXZQName = lastXZQName;
    }

    public String getXZQName() {
        return XZQName;
    }

    public void setXZQName(String XZQName) {
        this.XZQName = XZQName;
    }

    public int getXZQNum() {
        return XZQNum;
    }

    public void setXZQNum(int XZQNum) {
        this.XZQNum = XZQNum;
    }
}
