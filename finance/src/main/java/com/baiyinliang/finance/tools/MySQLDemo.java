package com.baiyinliang.finance.tools;


import java.sql.*;

public class MySQLDemo {

    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
    //static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    //static final String DB_URL = "jdbc:mysql://localhost:3306/runoob?characterEncoding=utf8&useSSL=true";

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    Connection conn = null;
    Statement stmt = null;
    static final String DB_URL = "jdbc:mysql://localhost:3306/bond_sys?useSSL=false&serverTimezone=UTC";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "root";
    public MySQLDemo() {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("加载数据库驱动成功");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("连接数据库驱动成功");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void query() {
        try{
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT id, name, url FROM websites";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int id  = rs.getInt("id");
                String name = rs.getString("name");
                String url = rs.getString("url");

                // 输出数据
                System.out.print("ID: " + id);
                System.out.print(", 站点名称: " + name);
                System.out.print(", 站点 URL: " + url);
                System.out.print("\n");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("");
    }
    public void insert() {

        String sql1 = "insert into websites(id,name,url,alexa,country) values(6,'java','www.baidu',6,'hmoe')";
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql1);
            ps.executeUpdate();
            System.out.println("插入成功！");

            System.out.println("插入结束！");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    //查询
    public void like(){

        String sql = "select * from hot where name like '%zhang%'";
        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            ps = (PreparedStatement) conn.prepareStatement(sql);
            res = ps.executeQuery();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {

            while(res.next()){

                int num = res.getInt(1);
                String name = res.getString(2);
                String author = res.getString(3);
                String style = res.getString(4);
                String form = res.getString(5);


                System.out.println("num: " + num + " ,name: " + name +" ,author: " + author + " ,style: " + style + " ,form: " + form);
                System.out.println("模糊查询成功");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    //更新
    public int update(){
        String sql = "update hot set name = '张军' where num=7";
        PreparedStatement ps = null;

        try {
            ps = (PreparedStatement) conn.prepareStatement(sql);
            ps.executeUpdate();
            System.out.println("更新成功！");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;

    }

    //删除
    public void delete(){

        String sql = "delete from hot where num = 1";
        String sql2 = "delete from hot where num = 2";
        String sql3 = "delete from hot where num = 3";
        String sql4 = "delete from hot where num = 4";
        String sql5 = "delete from hot where num = 5";

        PreparedStatement ps = null;

        try {
            ps = (PreparedStatement) conn.prepareStatement(sql);
            ps.executeUpdate();
            System.out.println("删除成功！");
            ps = (PreparedStatement) conn.prepareStatement(sql2);
            ps.executeUpdate();
            System.out.println("删除成功！");
            ps = (PreparedStatement) conn.prepareStatement(sql3);
            ps.executeUpdate();
            System.out.println("删除成功！");
            ps = (PreparedStatement) conn.prepareStatement(sql4);
            ps.executeUpdate();
            System.out.println("删除成功！");
            ps = (PreparedStatement) conn.prepareStatement(sql5);
            ps.executeUpdate();
            System.out.println("删除成功！");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public static void main(String[] args){

        MySQLDemo conn2 = new  MySQLDemo();
        conn2.insert();
        System.out.println("--------------------");
        conn2.like();
        System.out.println("--------------------");
        conn2.update();
        System.out.println("--------------------");
//		conn2.delete();
        System.out.println("--------------------");
        conn2.query();
        System.out.println("--------------------");
    }

}