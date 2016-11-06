package com.example.laijiahao.mychat.domain;

/**
 * Created by laijiahao on 16/10/28.
 */

/**
 * 对应文件夹
 */
public class FolderBean {

    /**
     * 当前文件夹路径
     */
    private String dir;

    /**
     * 当前第一张图片的路径
     */
    private String firstImagePath;

    /**
     * 当前文件夹的名称
     */
    private String name;

    /**
     * 当前文件夹中图片的数量
     */
    private int count;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf+1);
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public String getName() {
        return name;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
