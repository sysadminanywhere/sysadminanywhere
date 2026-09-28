const storedTheme = window.localStorage.getItem('theme');
const theme = storedTheme === 'dark' ? 'dark' : 'light';

document.documentElement.setAttribute('theme', theme);
