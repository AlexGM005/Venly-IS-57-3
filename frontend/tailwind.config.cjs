module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        venly: {
          primary: '#7c3aed',
          dark: '#1f2937'
        }
      }
    }
  },
  plugins: [require('@tailwindcss/forms')]
};
