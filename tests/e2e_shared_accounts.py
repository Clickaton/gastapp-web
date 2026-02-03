from playwright.sync_api import sync_playwright, expect
import time
import uuid
import base64
import os
import re

def verify_full_flow():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        user_a_email = f"usera_{uuid.uuid4()}@example.com".lower()
        user_b_email = f"userb_{uuid.uuid4()}@example.com".lower()
        password = "password"

        # Helper to register user
        def register_user(name, email, pwd):
            print(f"Registering {name} with {email}")
            page.goto("http://localhost:8080/register")
            page.fill("input[name='nombre']", name)
            page.fill("input[name='email']", email)
            page.fill("input[name='password']", pwd)
            page.click("button[type='submit']")
            expect(page).to_have_url("http://localhost:8080/login")

        # Helper to login
        def login(email, pwd):
            print(f"Logging in {email}")
            page.goto("http://localhost:8080/login")
            page.fill("input[name='username']", email)
            page.fill("input[name='password']", pwd)
            page.click("button[type='submit']")
            expect(page).to_have_url("http://localhost:8080/dashboard")

        # Create a dummy image for upload
        def create_dummy_image(path):
            # Create a 1x1 white pixel png
            img_data = base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVR4nGNiAAAABgDNjd8qAAAAAElFTkSuQmCC")
            with open(path, "wb") as f:
                f.write(img_data)

        try:
            create_dummy_image("/tmp/profile_pic.png")

            # 1. Register User A & B
            register_user("User A", user_a_email, password)
            register_user("User B", user_b_email, password)

            # 2. Login User A, Profile Update
            login(user_a_email, password)

            print("Updating Profile...")
            page.goto("http://localhost:8080/profile")
            page.fill("input[name='nombre']", "User A Updated")
            page.set_input_files("input[type='file']", "/tmp/profile_pic.png")
            page.click("button:has-text('Guardar cambios')")

            expect(page.locator("text=Perfil actualizado correctamente.")).to_be_visible()

            # Verify Image Upload logic (indirectly via UI, can also check FS)
            # Verify Dashboard update
            page.goto("http://localhost:8080/dashboard")
            expect(page.locator("text=Hola, User A Updated")).to_be_visible()
            # Verify image src contains /uploads/
            img_src = page.locator("img[alt='Perfil']").first.get_attribute("src")
            print(f"Image Source: {img_src}")
            if "/uploads/" not in img_src:
                raise Exception("Image source does not contain /uploads/")

            # Create Shared Account
            print("Creating Shared Account...")
            page.goto("http://localhost:8080/accounts/new")
            page.fill("input[name='nombre']", "Cuenta Compartida")
            page.select_option("select[name='tipo']", value="EFECTIVO")
            page.fill("input[name='saldoInicial']", "1000")
            page.check("#isShared")
            page.fill("input[name='sharedUserEmail']", user_b_email)
            page.click("button:has-text('Guardar')")

            # Logout
            page.click("button:has-text('Cerrar sesión')")
            context.clear_cookies()
            page.goto("http://localhost:8080/login")

            # 3. Login User B
            login(user_b_email, password)

            # Verify Account visible and Detail View
            print("Verifying Account Detail...")
            page.goto("http://localhost:8080/accounts")
            # Click Account Name to go to detail
            page.click("text=Cuenta Compartida")

            # Check for Split info (Should be 0 expenses initially)
            expect(page.locator("text=División de Gastos")).to_be_visible()
            expect(page.locator("text=Monto por persona")).to_be_visible()

            # Add Expense
            print("Adding expense...")
            page.goto("http://localhost:8080/expenses/new")
            page.fill("input[name='monto']", "500")
            page.fill("input[name='descripcion']", "Gasto de B")

            # Create Category if needed (User B needs category)
            # Check if category list is empty? User B has no categories.
            # Create category first.
            page.goto("http://localhost:8080/categories/new")
            page.fill("input[name='nombre']", "General")
            page.click("button:has-text('Guardar')")

            page.goto("http://localhost:8080/expenses/new")
            page.fill("input[name='monto']", "500")
            page.fill("input[name='descripcion']", "Gasto de B")
            # Date should be auto-filled
            date_val = page.input_value("input[name='fecha']")
            if not date_val:
                raise Exception("Date not autofilled")

            page.select_option("select[name='categoryId']", label="General")
            page.select_option("select[name='accountId']", label="Cuenta Compartida")
            page.click("button:has-text('Guardar')")

            # Verify Split Amount in Detail
            page.goto("http://localhost:8080/accounts")
            page.click("text=Cuenta Compartida")
            # Total = 500. Split (2 people) = 250.
            # Use Regex to allow "500.00 $" or "500,00 $"
            expect(page.get_by_text(re.compile(r"500[.,]00 \$"))).to_be_visible() # Total
            expect(page.get_by_text(re.compile(r"250[.,]00 \$"))).to_be_visible() # Split

            # Logout
            page.click("button:has-text('Cerrar sesión')")
            context.clear_cookies()

            print("Verification successful!")
            page.screenshot(path="/home/jules/verification/verification_full.png", full_page=True)

        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="/home/jules/verification/failure_full.png")
            raise e
        finally:
            browser.close()
            if os.path.exists("/tmp/profile_pic.png"):
                os.remove("/tmp/profile_pic.png")

if __name__ == "__main__":
    verify_full_flow()
