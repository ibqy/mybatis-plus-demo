<script setup>
import DefaultTheme from 'vitepress/theme'

const { Layout } = DefaultTheme
</script>

<template>
  <Layout>
    <template #home-features-before>
      <section class="sig">
        <div class="sig-card">
          <span class="sig-label">COURSE · 10 LESSONS</span>
          <h3 class="sig-title">从会用到懂原理</h3>
          <div class="mp-grid">
            <span class="mp-st"><b>01</b><i>数据库SQL</i></span>
            <span class="mp-st"><b>02</b><i>项目架构</i></span>
            <span class="mp-st"><b>03</b><i>实体映射</i></span>
            <span class="mp-st"><b>04</b><i>通用CRUD</i></span>
            <span class="mp-st mp-mil"><b>05</b><i>Lambda查询</i></span>
            <span class="mp-st"><b>06</b><i>事务边界</i></span>
            <span class="mp-st"><b>07</b><i>高级特性</i></span>
            <span class="mp-st"><b>08</b><i>自定义SQL</i></span>
            <span class="mp-st mp-mil"><b>09</b><i>实现原理</i></span>
            <span class="mp-st mp-mil"><b>10</b><i>代码生成</i></span>
          </div>
          <p class="sig-note">10 节成体系课件 + 6 个渐进式 Demo，按学习曲线编排而非 API 罗列——第 09 节会扒开 BaseMapper 的动态代理，看 SQL 是怎么被注入的。</p>
        </div>
      </section>
    </template>
    <template #home-features-after>
      <div class="sig-more"><a href="https://ibqy.github.io/" target="_blank" rel="noopener">← ibqy.github.io · 探索全部项目</a></div>
    </template>
  </Layout>
</template>
