export default {
  darkMode: "class",
  important: ":where(#app, #modals)",
  content: ["./index.html", "./src/**/*.{vue,ts,tsx,js,jsx}"],
  theme: {
    extend: {
      colors: {
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        card: "hsl(var(--card))",
      },
    },
  },
  plugins: [],
};
