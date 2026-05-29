import { Navigate, Route, Routes } from "react-router-dom";
import { Spin } from "antd";
import { useAuth } from "./auth/AuthContext";
import MainLayout from "./components/MainLayout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import PersonnelPage from "./pages/PersonnelPage";
import EquipmentPage from "./pages/EquipmentPage";
import { MaterialsPage, SuppliersPage } from "./pages/WarehousePages";
import DocumentsPage from "./pages/DocumentsPage";
import {
  EnvironmentPage,
  MethodsPage,
  ReportsPage,
  ResourcesPage,
} from "./pages/ModulePages";
import UsersPage from "./pages/UsersPage";
import type { ReactNode } from "react";

function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading)
    return (
      <div style={{ display: "flex", justifyContent: "center", marginTop: 120 }}>
        <Spin size="large" />
      </div>
    );
  if (!user) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <RequireAuth>
            <MainLayout />
          </RequireAuth>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/personnel" element={<PersonnelPage />} />
        <Route path="/equipment" element={<EquipmentPage />} />
        <Route path="/resources" element={<ResourcesPage />} />
        <Route path="/warehouse/materials" element={<MaterialsPage />} />
        <Route path="/warehouse/suppliers" element={<SuppliersPage />} />
        <Route path="/documents" element={<DocumentsPage />} />
        <Route path="/environment" element={<EnvironmentPage />} />
        <Route path="/methods" element={<MethodsPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/system/users" element={<UsersPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
