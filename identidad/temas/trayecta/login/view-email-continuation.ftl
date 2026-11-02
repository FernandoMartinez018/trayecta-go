<#-- Copia de view-email-continuation.ftl de keycloak-magic-link 0.94 con un cambio:
     al confirmarse el enlace no se redirige sola (Chrome bloquea abrir la app sin un toque
     si la pestaña está oculta); se muestra el botón «Continuar». -->
<#import "template.ftl" as layout>
<@layout.registrationLayout displayRequiredFields=false displayMessage=false; section>
 <#if section = "header">
    <div id="kc-username" class="${properties.kcFormGroupClass!}">
      <label id="kc-attempted-username">${msg("loginAccountTitle")}</label>
      <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
        <div class="kc-login-tooltip">
          <i class="${properties.kcResetFlowIcon!}"></i>
          <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
        </div>
      </a>
    </div>
  <#elseif section = "form">
    <div id="mlc-status">${msg("magicLinkContinuationWaiting")!msg("magicLinkContinuationConfirmation")}</div>
    <div id="mlc-exp"></div>
    <a id="mlc-continue" hidden href="${url.loginAction}" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!}">${msg("doContinue")}</a>
    <script>
      (function(){
        const pollingUrl = "${pollingUrl!""}";
        const statusEl = document.getElementById("mlc-status");
        const expEl = document.getElementById("mlc-exp");
        let stopped = false;

        async function tick(){
          if(stopped) return;
          try{
            const r = await fetch(pollingUrl, {
              cache: "no-store",
              credentials: "same-origin"
            });

            if(!r.ok){
              if(r.status === 404 || r.status === 401){
                stopped = true;
                statusEl.textContent = "${msg("magicLinkContinuationExpired")!"Your link has expired. Please request a new one."}";
                expEl.textContent = "";
                return;
              }
              throw new Error("status:" + r.status);
            }

            const data = await r.json();

            if(typeof data.expires_in === "number"){
              expEl.textContent = data.expires_in > 0
                ? "${msg("magicLinkContinuationExpiresIn")!"Expires in"} " + data.expires_in + "s"
                : "";
            }

            switch(data.state){
              case "confirmed":
                stopped = true;
                statusEl.textContent = "${msg("trayectaMlcConfirmed")}";
                expEl.textContent = "";
                document.getElementById("mlc-continue").hidden = false;
                return;
              case "expired":
                stopped = true;
                statusEl.textContent = "${msg("magicLinkContinuationExpired")!"Your link has expired. Please request a new one."}";
                expEl.textContent = "";
                return;
              default: // pending
                setTimeout(tick, 2500);
            }
          } catch(e){
            // Error de red o servidor no disponible: se reintenta más despacio.
            setTimeout(tick, 4000);
          }
        }
        tick();
      })();
  </script>
  </#if>
</@layout.registrationLayout>
