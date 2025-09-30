/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}", // Ensure Tailwind processes your JSX and TSX files
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Roboto", "sans-serif"], // Default sans-serif font
        lexend: ["Lexend", "sans-serif"], // Lexend font
        manrope: ["Manrope", "sans-serif"], // Manrope font
        raleway: ["Raleway", "sans-serif"], // Raleway font
        ledger: ["Ledger", "serif"],
      },
      colors: {
        'blue-013171': '#013171',
      },
    },
  },
  plugins: [],
};
