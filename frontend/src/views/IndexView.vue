<script lang="ts" setup>
import router from "@/router";
import {logout} from "@/net";

import {ref} from 'vue'

const activeIndex = ref('1')
const handleSelect = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}

function userLogout() {
  logout(() => router.push("/"))
}

function jumpToAnime() {
  window.location.replace("https://anime.bang-dream.com/mygo/")
}

function jumpToGame() {
  window.location.replace("https://bang-dream.com/mygo")
}

function jumpToMusic() {
  window.location.replace("https://www.bilibili.com/video/BV1mC4y1f7HC/?share_source=copy_web&vd_source=21f785bb4341525c57165a8a60a0d293")
}

</script>

<template>
  <div class="common-layout">
    <el-container class="el-container">
      <el-header style="background-color:#cad7dd">
        <el-menu :default-active="activeIndex"
                 class="el-menu-demo"
                 mode="horizontal"
                 :ellipsis="false"
                 @select="handleSelect"
                 background-color="#cad7dd">
          <el-menu-item index="0">
            <img style="width:90px;margin-top:10px;margin-bottom:0;"
                 src="/logo_mygo.ico"
                 alt="MyGO logo"/>
          </el-menu-item>
          <div class="font-set"/>
          <el-menu-item index="1" @click="jumpToAnime" class="font-set">动画</el-menu-item>
          <el-menu-item index="2" @click="jumpToGame" class="font-set">游戏</el-menu-item>
          <el-menu-item index="3" @click="jumpToMusic" class="font-set">音乐</el-menu-item>
          <el-menu-item index="4" @click="userLogout" class="font-set">退出登录</el-menu-item>
          <div/>
        </el-menu>
      </el-header>
      <el-main style="background-color: #0b2637">
        <div style="text-align:center;font-size: 18px;color:#3288bb;font-weight: bold">
          迷子でもいい、迷子でも進め。
        </div>
        <div style="text-align:center;font-size: 18px;color:#3288bb;font-weight: bold">
          不畏迷茫，迷茫着也要砥砺前行。
        </div>
        <div style="margin-top: 20px">
          <el-image src="/anime_MyGO_kabegami_0623_PC.jpg"/>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.font-set {
  font-size: 20px;
  font-weight: bold;
  color: #3288bb;
}

</style>