<script setup>
import {reactive, ref} from 'vue'
import {Lock, User} from '@element-plus/icons'
import {login} from "@/net";
import router from "@/router";

// 可以通过formRef来获取form里的值
const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rule = {
  username: [
    {required: true, message: '请输入用户名/邮箱', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
  ]
}

function userLogin() {
  formRef.value.validate((isValid) => {
    if (isValid) {
      login(form.username, form.password, form.remember, () => router.push("/index"))
    }
  })
}
</script>

<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 15px;color:grey">进入官网之前，请先登录</div>
    </div>
    <div style="margin-top: 30px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="20" type="text" placeholder="用户名/邮箱">
            <template #prefix>
              <el-icon>
                <User/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
            <template #prefix>
              <el-icon>
                <Lock/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-row style="margin-top: 5px">
          <el-col :span="12" style="text-align: left">
            <el-form-item prop="remember">
              <el-checkbox v-model="form.remember" label="记住我"/>
            </el-form-item>
          </el-col>
          <el-col :span="12" style="text-align: right">
            <el-link @click="router.push('/forget')">忘记密码？</el-link>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div style="margin-top: 30px">
      <el-button @click="userLogin()" style="width: 250px" type="success" plain>登录</el-button>
    </div>
    <el-divider>
      <span style="font-size: 10px;color: grey">没有账号？</span>
    </el-divider>
    <div>
      <el-button @click="router.push('/register')" style="width: 250px" type="warning" plain>注册</el-button>
    </div>
  </div>
</template>

<style scoped>

</style>