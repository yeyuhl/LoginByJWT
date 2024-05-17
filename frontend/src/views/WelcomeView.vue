<script setup>

</script>

<template>
  <div style="width: 100vw;height:100vh;overflow: hidden;display: flex">
    <div style="flex: 1;background-color: lavenderblush">
      <el-image style="width: 100%;height: 100%" fit="cover"
                src="https://yeyu-1313730906.cos.ap-guangzhou.myqcloud.com/%E7%89%B9%E5%AE%9A%E6%96%87%E4%BB%B6/badf875b0af20cc431f217ac17f77c69136107.jpg"/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到MyGO!!!!!官网</div>
    </div>
    <div class="right-card">
      <router-view/>
    </div>
  </div>
</template>

<style scoped>
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
}

.welcome-title {
  position: absolute;
  bottom: 30px;
  left: 30px;
  font-family: "Microsoft YaHei", sans-serif;
  color: skyblue;
  text-shadow: 0 0 10px black;
}
</style>