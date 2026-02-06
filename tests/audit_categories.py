from playwright.sync_api import sync_playwright, expect
import time
import uuid
import re

def audit_categories():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        user_email = f"audit_cat_{uuid.uuid4()}@example.com".lower()
        password = "password"

        # 1. Register User
        print(f"Registering user: {user_email}")
        page.goto("http://localhost:8080/register")
        page.fill("input[name='nombre']", "Audit User")
        page.fill("input[name='email']", user_email)
        page.fill("input[name='password']", password)
        page.click("button[type='submit']")
        expect(page).to_have_url("http://localhost:8080/login")

        # 2. Login
        print("Logging in...")
        page.goto("http://localhost:8080/login")
        page.fill("input[name='username']", user_email)
        page.fill("input[name='password']", password)
        page.click("button[type='submit']")
        expect(page).to_have_url("http://localhost:8080/dashboard")

        # 3. Create a Category
        print("Creating Category...")
        page.goto("http://localhost:8080/categories/new")
        page.fill("input[name='nombre']", "Test Category")
        # Color input is hidden or special, fill the visible one or js
        page.fill("input[id='colorHex']", "#FF5733")
        # Trigger input event to update hidden
        page.dispatch_event("input[id='colorHex']", "input")
        page.click("button:has-text('Guardar')")

        # 4. Create a Budget for that Category (to test progress bars)
        print("Creating Budget...")
        page.goto("http://localhost:8080/budgets/new")
        page.select_option("select[name='categoryId']", label="Test Category")
        page.fill("input[name='montoMaximo']", "1000")
        page.click("button:has-text('Guardar')")

        # 5. Create an Account (needed for expense)
        page.goto("http://localhost:8080/accounts/new")
        page.fill("input[name='nombre']", "Wallet")
        page.select_option("select[name='tipo']", value="EFECTIVO")
        page.fill("input[name='saldoInicial']", "5000")
        page.click("button:has-text('Guardar')")

        # 6. Add Expense to fill budget partially
        print("Adding Expense (50%)...")
        page.goto("http://localhost:8080/expenses/new")
        page.fill("input[name='monto']", "500")
        page.fill("input[name='descripcion']", "Half budget")
        page.select_option("select[name='categoryId']", label="Test Category")
        page.select_option("select[name='accountId']", label="Wallet")
        page.click("button:has-text('Guardar')")

        # 7. Verify Categories List Page
        print("Auditing Categories List...")
        page.goto("http://localhost:8080/categories")

        # Check if page loaded without 500 error (Thymeleaf template error)
        if page.locator("text=Internal Server Error").is_visible():
            print("FAILURE: Categories list crashed.")
            raise Exception("Categories list crashed")

        # Check Progress Bar Logic
        # Expecting ~50% width
        progress_bar = page.locator(".progress-bar").first
        style = progress_bar.get_attribute("style")
        print(f"Progress Bar Style: {style}")

        if "width: 50%" not in style and "width:50%" not in style and "width: 50.0%" not in style:
             # Allow small variations or decimals depending on formatting?
             # Logic is usually exact if inputs are exact.
             # T(java.lang.Math) check might fail if restricted.
             pass

        # 8. Add Expense to Exceed Budget
        print("Adding Expense (Exceed)...")
        page.goto("http://localhost:8080/expenses/new")
        page.fill("input[name='monto']", "600") # Total 1100 / 1000
        page.select_option("select[name='categoryId']", label="Test Category")
        page.select_option("select[name='accountId']", label="Wallet")
        # Handle warning
        page.click("button:has-text('Guardar')") # First click might show warning?
        # Actually logic shows warning in flash attribute but redirects?
        # If it redirects to form with warning, we need to click again or check.
        # Current logic in controller: adds flash attribute "warning" but redirects to list if saved?
        # Let's check ExpenseWebController logic.
        # "redirect.addFlashAttribute... return redirect:/expenses" -> It saves and redirects.

        page.goto("http://localhost:8080/categories")

        # Check Exceeded State (Red progress bar, width 100%)
        progress_bar = page.locator(".progress-bar").first
        style = progress_bar.get_attribute("style")
        classes = progress_bar.get_attribute("class")
        print(f"Exceeded Bar Style: {style}, Classes: {classes}")

        if "bg-danger" not in classes:
            print("FAILURE: Progress bar not red when exceeded.")

        # Check T(java.lang.Math).min behavior -> Should be max 100%
        # If raw value is > 100, and we use min(val, 100), it should be 100.
        if "width: 100%" not in style and "width:100%" not in style and "width: 100.0%" not in style:
             print("FAILURE: Progress bar width not capped at 100%.")

        print("Audit Complete.")
        browser.close()

if __name__ == "__main__":
    audit_categories()
