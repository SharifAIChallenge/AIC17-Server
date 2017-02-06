package Swarm.models;

import java.io.Serializable;

/**
 * Copyright (C) 2016 Hadi
 */
public class GameConstants implements Serializable{

    private long turnTimeout;
    private int w;
    private int h;
    private int teamNum = 2;
    private int teleportNum;
    private int initFishNum;
    private int initQueenNum;

    private double foodProb;
    private double trashProb;
    private double netProb;

    private int netActiveTime;
    private int netValidTime;

    //

   // private int changeColorLimit;
   // private int kStep;
    // or :
    private int colorCost;
    //
    private int sickCost;
    private int UpdateCost;
    private int detMoveCost;

    private int killQueenScore;
    private int killBothQueenScore;
    private int killFishScore;
    private int queenCollisionScore;
    private int fishFoodScore;
    private int queenFoodScore;
    private int sickLifeTime;

    private int powerRatio;
    private double endRatio;

    private int disobeyPointerTeam0;
    private int disobeyPointerTeam1;
    private int disobeyNum;
    private double disobeyRatio;

    private int foodValidTime;
    private int trashValidTime;



    public long getTurnTimeout() {
        return turnTimeout;
    }

    public void setTurnTimeout(long turnTimeout) {
        this.turnTimeout = turnTimeout;
    }



    public int getTeamNum() {
        return teamNum;
    }

    public void setTeamNum(int teamNum) {
        this.teamNum = teamNum;
    }

    public int getTeleportNum() {
        return teleportNum;
    }

    public void setTeleportNum(int teleportNum) {
        this.teleportNum = teleportNum;
    }

    public int getInitFishNum() {
        return initFishNum;
    }

    public void setInitFishNum(int initFishNum) {
        this.initFishNum = initFishNum;
    }

    public int getInitQueenNum() {
        return initQueenNum;
    }

    public void setInitQueenNum(int initQueenNum) {
        this.initQueenNum = initQueenNum;
    }

    public double getFoodProb() {
        return foodProb;
    }

    public void setFoodProb(double foodProb) {
        this.foodProb = foodProb;
    }

    public double getTrashProb() {
        return trashProb;
    }

    public void setTrashProb(double trashProb) {
        this.trashProb = trashProb;
    }

    public double getNetProb() {
        return netProb;
    }

    public void setNetProb(double netProb) {
        this.netProb = netProb;
    }

    public int getNetActiveTime() {
        return netActiveTime;
    }

    public void setNetActiveTime(int netActiveTime) {
        this.netActiveTime = netActiveTime;
    }

    public int getNetValidTime() {
        return netValidTime;
    }

    public void setNetValidTime(int netValidTime) {
        this.netValidTime = netValidTime;
    }

    public int getColorCost() {
        return colorCost;
    }

    public void setColorCost(int colorCost) {
        this.colorCost = colorCost;
    }

    public int getSickCost() {
        return sickCost;
    }

    public void setSickCost(int sickCost) {
        this.sickCost = sickCost;
    }

    public int getUpdateCost() {
        return UpdateCost;
    }

    public void setUpdateCost(int updateCost) {
        UpdateCost = updateCost;
    }

    public int getDetMoveCost() {
        return detMoveCost;
    }

    public void setDetMoveCost(int detMoveCost) {
        this.detMoveCost = detMoveCost;
    }

    public int getKillQueenScore() {
        return killQueenScore;
    }

    public void setKillQueenScore(int killQueenScore) {
        this.killQueenScore = killQueenScore;
    }

    public int getKillFishScore() {
        return killFishScore;
    }

    public void setKillFishScore(int killFishScore) {
        this.killFishScore = killFishScore;
    }

    public int getQueenCollisionScore() {
        return queenCollisionScore;
    }

    public void setQueenCollisionScore(int queenCollisionScore) {
        this.queenCollisionScore = queenCollisionScore;
    }

    public int getFishFoodScore() {
        return fishFoodScore;
    }

    public void setFishFoodScore(int fishFoodScore) {
        this.fishFoodScore = fishFoodScore;
    }

    public int getQueenFoodScore() {
        return queenFoodScore;
    }

    public void setQueenFoodScore(int queenFoodScore) {
        this.queenFoodScore = queenFoodScore;
    }

    public int getSickLifeTime() {
        return sickLifeTime;
    }

    public void setSickLifeTime(int sickLifeTime) {
        this.sickLifeTime = sickLifeTime;
    }

    public int getPowerRatio() {
        return powerRatio;
    }

    public void setPowerRatio(int powerRatio) {
        this.powerRatio = powerRatio;
    }

    public double getEndRatio() {
        return endRatio;
    }

    public void setEndRatio(double endRatio) {
        this.endRatio = endRatio;
    }

    public int getDisobeyPointerTeam0() {
        return disobeyPointerTeam0;
    }

    public void setDisobeyPointerTeam0(int disobeyPointerTeam0) {
        this.disobeyPointerTeam0 = disobeyPointerTeam0;
    }

    public int getDisobeyPointerTeam1() {
        return disobeyPointerTeam1;
    }

    public void setDisobeyPointerTeam1(int disobeyPointerTeam1) {
        this.disobeyPointerTeam1 = disobeyPointerTeam1;
    }

    public int getDisobeyNum() {
        return disobeyNum;
    }

    public void setDisobeyNum(int disobeyNum) {
        this.disobeyNum = disobeyNum;
    }

    public double getDisobeyRatio() {
        return disobeyRatio;
    }

    public void setDisobeyRatio(double disobeyRatio) {
        this.disobeyRatio = disobeyRatio;
    }

    public int getFoodValidTime() {
        return foodValidTime;
    }

    public void setFoodValidTime(int foodValidTime) {
        this.foodValidTime = foodValidTime;
    }

    public int getTrashValidTime() {
        return trashValidTime;
    }

    public void setTrashValidTime(int trashValidTime) {
        this.trashValidTime = trashValidTime;
    }

    public int getKillBothQueenScore() {
        return killBothQueenScore;
    }

    public void setKillBothQueenScore(int killBothQueenScore) {
        this.killBothQueenScore = killBothQueenScore;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

}
