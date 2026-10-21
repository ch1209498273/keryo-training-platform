"""Repeatable API smoke test for the isolated baseline database."""
import json
import os
import sys
import time
import urllib.error
import urllib.request
import uuid


BASE = os.environ.get("BASELINE_API_URL", "http://127.0.0.1:9092").rstrip("/")
EXAM_ID = "1915227067167539202"
STUDENT_ID = "1914226980618383361"


def required_credential(name):
    value = os.environ.get(name)
    if not value:
        raise RuntimeError(f"Set {name} in the local environment before running API smoke")
    return value


def request(method, path, body=None, token=None):
    data = None if body is None else json.dumps(body).encode("utf-8")
    req = urllib.request.Request(BASE + path, data=data, method=method)
    if data is not None:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("token", token)
    try:
        with urllib.request.urlopen(req, timeout=30) as response:
            raw = response.read()
            return json.loads(raw.decode("utf-8")) if raw else {}
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8", errors="replace")
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            return {"code": exc.code, "msg": raw}


def require(label, condition, detail=""):
    if not condition:
        raise RuntimeError(f"{label}: {detail}")
    print(f"PASS {label}")


def login(username, password):
    key = str(uuid.uuid4())
    with urllib.request.urlopen(BASE + f"/api/common/captcha/gen?key={key}", timeout=30) as response:
        response.read()
    captcha = request("GET", f"/api/common/captcha/debug?key={key}")
    require(f"captcha {username}", captcha.get("code") == 0 and captcha.get("data"))
    result = request(
        "POST",
        "/api/sys/user/login",
        {"userName": username, "password": password, "captchaKey": key, "captchaValue": captcha["data"]},
    )
    require(f"login {username}", result.get("code") == 0 and result.get("data", {}).get("token"), str(result))
    return result["data"]["token"]


def main():
    admin = login("admin", required_credential("BASELINE_ADMIN_PASSWORD"))
    manager = login("nurse_head", required_credential("BASELINE_MANAGER_PASSWORD"))
    student = login("student", required_credential("BASELINE_STUDENT_PASSWORD"))

    require("admin info", request("POST", "/api/sys/user/info", {"token": admin}, admin).get("code") == 0)
    require("manager info", request("POST", "/api/sys/user/info", {"token": manager}, manager).get("code") == 0)
    require("student info", request("POST", "/api/sys/user/info", {"token": student}, student).get("code") == 0)

    training = request("POST", "/api/keryo/training/paging", {"current": 1, "size": 10, "params": {}}, manager)
    require("training paging", training.get("code") == 0 and training.get("data", {}).get("records"), str(training))
    mine = request("POST", "/api/keryo/training/my", {}, student)
    require("student training list", mine.get("code") == 0 and mine.get("data"), str(mine))

    plans = request("POST", "/api/keryo/training-plan/paging", {"current": 1, "size": 10, "params": {}}, manager)
    require("manager plan paging", plans.get("code") == 0 and plans.get("data", {}).get("records"), str(plans))
    detail = request("POST", "/api/keryo/training-plan/detail", {"id": "baseline_plan"}, student)
    require("student plan detail", detail.get("code") == 0 and detail.get("data", {}).get("id") == "baseline_plan", str(detail))

    participants = request("POST", "/api/keryo/training-plan/participants", {"planId": "baseline_plan"}, manager)
    require("plan participants", participants.get("code") == 0 and participants.get("data"), str(participants))

    run_id = f"baseline_{int(time.time())}"
    created = request(
        "POST",
        "/api/keryo/training-plan/save",
        {
            "id": run_id,
            "title": run_id,
            "content": "repeatable smoke flow",
            "examId": EXAM_ID,
            "coursewareId": "baseline_training",
            "status": "DRAFT",
            "startTime": "2020-01-01 00:00:00",
            "endTime": "2050-12-31 23:59:59",
        },
        manager,
    )
    require("manager creates plan", created.get("code") == 0 and created.get("data") == run_id, str(created))
    require("manager assigns student", request("POST", "/api/keryo/training-plan/assign", {"planId": run_id, "userIds": [STUDENT_ID]}, manager).get("code") == 0)
    require("manager submits plan", request("POST", "/api/keryo/training-plan/submit", {"id": run_id}, manager).get("code") == 0)
    require("admin approves plan", request("POST", "/api/keryo/training-plan/approve", {"id": run_id, "remark": "baseline"}, admin).get("code") == 0)

    exams = request("POST", "/api/exam/exam/exam/client-paging", {"current": 1, "size": 10, "params": {}}, student)
    require("student exam list", exams.get("code") == 0 and exams.get("data", {}).get("records"), str(exams))
    precheck = request("POST", "/api/exam/paper/paper/pre-check", {"id": EXAM_ID}, student)
    require("exam pre-check", precheck.get("code") == 0 and precheck.get("data", {}).get("validated") is True, str(precheck))
    paper = request("POST", "/api/exam/paper/paper/create", {"id": EXAM_ID}, student)
    paper_id = paper.get("data", {}).get("id")
    require("create paper", paper.get("code") == 0 and paper_id, str(paper))
    card = request("POST", "/api/exam/paper/qu/list-card", {"id": paper_id}, student)
    require("load answer card", card.get("code") == 0 and card.get("data"), str(card))
    handed = request("POST", "/api/exam/paper/paper/hand", {"id": paper_id}, student)
    require("submit paper", handed.get("code") == 0, str(handed))
    records = request("POST", "/api/exam/exam/record/client-paging", {"current": 1, "size": 10, "params": {}}, student)
    require("student result list", records.get("code") == 0 and records.get("data", {}).get("records"), str(records))
    require("logout", request("POST", "/api/sys/user/logout", {}, student).get("code") == 0)
    print("BASELINE_SMOKE_PASS")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"BASELINE_SMOKE_FAIL {exc}", file=sys.stderr)
        sys.exit(1)

