def test_health(client):
    resp = client.get("/api/health")
    assert resp.status_code == 200
    assert resp.json()["status"] == "ok"


def test_login_and_me(client, auth_headers):
    resp = client.get("/api/v1/auth/me", headers=auth_headers)
    assert resp.status_code == 200
    data = resp.json()
    assert data["username"] == "admin"
    assert data["is_superuser"] is True
    assert "*" in data["permissions"]


def test_login_bad_password(client):
    resp = client.post(
        "/api/v1/auth/login", data={"username": "admin", "password": "wrong"}
    )
    assert resp.status_code == 401


def test_requires_auth(client):
    assert client.get("/api/v1/personnel").status_code == 401


def test_personnel_crud(client, auth_headers):
    create = client.post(
        "/api/v1/personnel",
        headers=auth_headers,
        json={"employee_no": "T001", "name": "Tester", "position": "QA"},
    )
    assert create.status_code == 201
    pid = create.json()["id"]

    listed = client.get("/api/v1/personnel", headers=auth_headers)
    assert listed.status_code == 200
    assert listed.json()["total"] >= 1

    updated = client.put(
        f"/api/v1/personnel/{pid}", headers=auth_headers, json={"position": "Lead"}
    )
    assert updated.status_code == 200
    assert updated.json()["position"] == "Lead"

    assert client.delete(f"/api/v1/personnel/{pid}", headers=auth_headers).status_code == 204


def test_equipment_and_qrcode(client, auth_headers):
    create = client.post(
        "/api/v1/equipment",
        headers=auth_headers,
        json={"asset_no": "T-EQ-1", "name": "Scale", "status": "idle"},
    )
    assert create.status_code == 201
    eid = create.json()["id"]

    qr = client.get(f"/api/v1/equipment/{eid}/qrcode", headers=auth_headers)
    assert qr.status_code == 200
    assert qr.headers["content-type"] == "image/png"


def test_dashboard_summary(client, auth_headers):
    resp = client.get("/api/v1/dashboard/summary", headers=auth_headers)
    assert resp.status_code == 200
    body = resp.json()
    assert "totals" in body
    assert "reminders" in body


def test_generic_module_crud(client, auth_headers):
    create = client.post(
        "/api/v1/documents",
        headers=auth_headers,
        json={"doc_no": "DOC-1", "title": "SOP", "status": "draft"},
    )
    assert create.status_code == 201
    listed = client.get("/api/v1/documents", headers=auth_headers)
    assert listed.json()["total"] >= 1
