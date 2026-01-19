// 清除 localStorage 中的 token 和用户信息（模拟）
console.log('Clearing localStorage tokens and user info...');

// 模拟 localStorage
const localStorage = {
  data: {},
  getItem(key) {
    return this.data[key] || null;
  },
  setItem(key, value) {
    this.data[key] = value;
  },
  removeItem(key) {
    delete this.data[key];
  }
};

// 模拟设置一些数据
localStorage.setItem('ADMIN_TOKEN', 'test-token');
localStorage.setItem('USER_TOKEN', 'test-user-token');

// 清除后台相关
localStorage.removeItem('ADMIN_TOKEN');
localStorage.removeItem('ADMIN_TOKEN_NAME');
localStorage.removeItem('ADMIN_ROLES');
localStorage.removeItem('ADMIN_PERMS');
localStorage.removeItem('ADMIN_USER');

// 清除前台相关
localStorage.removeItem('USER_TOKEN');
localStorage.removeItem('USER_INFO');

console.log('LocalStorage cleared successfully!');
console.log('ADMIN_TOKEN:', localStorage.getItem('ADMIN_TOKEN'));
console.log('USER_TOKEN:', localStorage.getItem('USER_TOKEN'));

console.log('\nNote: This is a simulation. To clear actual localStorage, open browser dev tools and run:');
console.log('localStorage.clear()');
