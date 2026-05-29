import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import enUS from "antd/locale/en_US";
import { useTranslation } from "react-i18next";
import "antd/dist/reset.css";
import App from "./App";
import "./i18n";
import { AuthProvider } from "./auth/AuthContext";

function Root() {
  const { i18n } = useTranslation();
  return (
    <ConfigProvider
      locale={i18n.language === "en" ? enUS : zhCN}
      theme={{ token: { colorPrimary: "#1677ff" } }}
    >
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>,
);
