/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        venly: {
          bg: '#0f0f0f',
          surface: '#1a1a2e',
          card: '#16213e',
          accent: '#e94560',
          gold: '#d4a843',
          text: '#eaeaea',
          muted: '#9ca3af',
          border: '#2a2f4a'
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif']
      },
      boxShadow: {
        glow: '0 0 24px -4px rgba(233, 69, 96, 0.45)',
        card: '0 20px 50px -20px rgba(0, 0, 0, 0.8)'
      },
      keyframes: {
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        pop: {
          '0%': { opacity: '0', transform: 'scale(0.85)' },
          '60%': { opacity: '1', transform: 'scale(1.05)' },
          '100%': { transform: 'scale(1)' }
        },
        draw: {
          '0%': { strokeDashoffset: '24' },
          '100%': { strokeDashoffset: '0' }
        }
      },
      animation: {
        'fade-up': 'fade-up 0.45s ease-out both',
        pop: 'pop 0.4s ease-out both',
        draw: 'draw 0.5s 0.15s ease-out both'
      }
    }
  },
  plugins: [require('@tailwindcss/forms')]
};
