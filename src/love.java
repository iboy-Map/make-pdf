public  class love {
    public static void main(String[] args)
    {
        try
        {
            System.out.println("public class love");
            System.out.println("{");
            System.out.println("    public static void main(String[] args)");
            System.out.println("    {");
            System.out.println("       try");
            System.out.println("       {");
            System.out.println("          System.out.println(\"该命题无解，祝君好运！\");");
            System.out.println("       }");
            System.out.println("       catch (Exception py)");
            System.out.println("       {");
            System.out.println("          System.out.println(\"py抛出了一个异常：\" + py.toString());");
            System.out.println("       }");
            System.out.println("    }");
            System.out.println("}");
        }
        catch (Exception py)
        {
            System.out.println(py.toString());
        }
    }
}
