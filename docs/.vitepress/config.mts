import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'zh-CN',
  title: 'MyBatis-Plus 从零到原理',
  description: '10 节成体系课件 + 6 个渐进式 Demo，从 Lambda 单表查询到代码生成器',
  base: '/mybatis-plus-demo/',
  lastUpdated: true,
  markdown: {
    config(md) {
      const defaultLink =
        md.renderer.rules.link_open ||
        ((tokens, idx, options, _env, self) => self.renderToken(tokens, idx, options))
      md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
        const href = tokens[idx].attrGet('href')
        if (href && href.startsWith('../')) {
          const rel = href.replace(/^(\.\.\/)+/, '')
          const kind = /\.[A-Za-z]+$/.test(rel) ? 'blob' : 'tree'
          tokens[idx].attrSet('href', `https://github.com/ibqy/mybatis-plus-demo/${kind}/main/${rel}`)
        }
        return defaultLink(tokens, idx, options, env, self)
      }
    }
  },
  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: 'GitHub', link: 'https://github.com/ibqy/mybatis-plus-demo' }
    ],
    sidebar: [
      {
        text: '教学文档',
        items: [
          { text: '00 · 课程目录', link: '/00-课程目录' },
          { text: '01 · 数据库与SQL基础', link: '/01-数据库与SQL基础' },
          { text: '02 · 项目架构与启动', link: '/02-项目架构与启动' },
          { text: '03 · 实体映射', link: '/03-实体映射' },
          { text: '04 · 通用CRUD', link: '/04-通用CRUD' },
          { text: '05 · Lambda单表查询', link: '/05-Lambda单表查询' },
          { text: '06 · Service事务与数据边界', link: '/06-Service事务与数据边界' },
          { text: '07 · 高级特性', link: '/07-高级特性' },
          { text: '08 · 自定义XML-SQL', link: '/08-自定义XML-SQL' },
          { text: '09 · MyBatis-Plus实现原理', link: '/09-MyBatisPlus实现原理' },
          { text: '10 · 代码生成器测试与排错', link: '/10-代码生成器测试与排错' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/ibqy/mybatis-plus-demo' }
    ],
    search: { provider: 'local' },
    outline: { level: [2, 3], label: '本页目录' },
    docFooter: { prev: '上一篇', next: '下一篇' },
    lastUpdated: { text: '最后更新于' },
    darkModeSwitchLabel: '外观',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式',
    sidebarMenuLabel: '文档',
    returnToTopLabel: '回到顶部',
    footer: {
      message: '个人教学项目 · 代码可跑 · 注释记录设计取舍',
      copyright: 'Copyright © 2026 ibqy'
    }
  }
})
