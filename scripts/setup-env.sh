#!/usr/bin/env bash
# Configura y persiste JDK 25 y OpenSpec CLI para BidMaster.
# Uso:
#   source scripts/setup-env.sh   # Activa JAVA_HOME y PATH en la sesión actual
#   ./scripts/setup-env.sh        # Instala/verifica herramientas y actualiza ~/.bashrc

_bidmaster_setup_env() {
  local tools_dir
  if [ -d "$HOME/.gemini" ]; then
    tools_dir="$HOME/.gemini/tools"
  else
    tools_dir="$HOME/.local/share/bidmaster-tools"
  fi

  local jdk_dir="$tools_dir/jdk-25"
  local bin_dir="$tools_dir/bin"
  mkdir -p "$tools_dir" "$bin_dir" "$HOME/.local/bin"

  # 1. Verificar o instalar JDK 25
  local current_java_major=""
  if command -v java >/dev/null 2>&1; then
    current_java_major=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d'.' -f1)
  fi

  if [ "$current_java_major" != "25" ]; then
    if [ ! -x "$jdk_dir/bin/java" ]; then
      local arch
      case "$(uname -m)" in
        x86_64|amd64) arch="x64" ;;
        aarch64|arm64) arch="aarch64" ;;
        *)
          echo "Arquitectura no soportada automáticamente para Temurin 25: $(uname -m)" >&2
          return 1
          ;;
      esac

      echo "Instalando Eclipse Temurin JDK 25 en $jdk_dir..."
      local tmp_tar="/tmp/bidmaster-jdk25.tar.gz"
      local tmp_extract="/tmp/bidmaster-jdk25-extract"
      rm -rf "$tmp_extract"
      mkdir -p "$tmp_extract" "$jdk_dir"
      curl -fsSL "https://api.adoptium.net/v3/binary/latest/25/ga/linux/${arch}/jdk/hotspot/normal/eclipse" -o "$tmp_tar" || return 1
      tar -xzf "$tmp_tar" -C "$tmp_extract" --strip-components=1 || return 1
      (
        cd "$tmp_extract" || exit 1
        find . -type d -exec mkdir -p "$jdk_dir/{}" \;
        find . -type f -exec sh -c 'cat "$1" > "$2/$1"' _ {} "$jdk_dir" \;
        chmod +x "$jdk_dir/bin/"* 2>/dev/null || true
      ) || return 1
      rm -rf "$tmp_tar" "$tmp_extract"
    fi

    export JAVA_HOME="$jdk_dir"
    case ":$PATH:" in
      *":$jdk_dir/bin:"*) ;;
      *) export PATH="$jdk_dir/bin:$PATH" ;;
    esac
  fi

  # 2. Verificar o instalar OpenSpec CLI (binario standalone)
  if ! command -v openspec >/dev/null 2>&1; then
    if [ ! -x "$bin_dir/openspec" ]; then
      echo "Instalando OpenSpec CLI standalone en $bin_dir..."
      if curl -fsSL https://openspec.dev/cli/install.sh | sh && [ -x "$HOME/.local/bin/openspec" ]; then
        cat "$HOME/.local/bin/openspec" > "$bin_dir/openspec" || return 1
      else
        local tmp_openspec="/tmp/bidmaster-openspec"
        rm -rf "$tmp_openspec"
        mkdir -p "$tmp_openspec"
        npm install --prefix "$tmp_openspec" @fission-ai/openspec@latest || return 1
        "$tmp_openspec/node_modules/.bin/openspec" install || return 1
        cat "$HOME/.local/bin/openspec" > "$bin_dir/openspec" || return 1
        rm -rf "$tmp_openspec"
      fi
      chmod +x "$bin_dir/openspec" 2>/dev/null || true
    fi

    case ":$PATH:" in
      *":$bin_dir:"*) ;;
      *) export PATH="$bin_dir:$PATH" ;;
    esac
  fi

  # Enlace conveniente en ~/.local/bin si aún no apunta al binario persistente
  if [ -x "$bin_dir/openspec" ] && [ ! -e "$HOME/.local/bin/openspec" ]; then
    ln -sf "$bin_dir/openspec" "$HOME/.local/bin/openspec"
  fi

  # 3. Truststore Java temporal para CAs locales (p. ej. proxy HTTPS en VMs sandbox)
  local active_java_home="${JAVA_HOME:-}"
  if [ -z "$active_java_home" ] && command -v java >/dev/null 2>&1; then
    active_java_home="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
  fi
  local base_cacerts="$active_java_home/lib/security/cacerts"
  local custom_cacerts="/tmp/bidmaster-cacerts"
  local ca_dir="/usr/local/share/ca-certificates"
  local has_local_ca=0
  local crt
  if [ -d "$ca_dir" ]; then
    for crt in "$ca_dir"/*.crt; do
      if [ -f "$crt" ]; then
        has_local_ca=1
        break
      fi
    done
  fi

  if [ "$has_local_ca" -eq 1 ] && [ -f "$base_cacerts" ]; then
    if [ ! -f "$custom_cacerts" ]; then
      cat "$base_cacerts" > "$custom_cacerts" && chmod 644 "$custom_cacerts"
      local alias_name
      for crt in "$ca_dir"/*.crt; do
        [ -f "$crt" ] || continue
        alias_name="bidmaster-$(basename "$crt" .crt)"
        "$active_java_home/bin/keytool" -importcert -noprompt -trustcacerts \
          -alias "$alias_name" -file "$crt" -keystore "$custom_cacerts" -storepass changeit >/dev/null 2>&1 || true
      done
    fi

    case " ${MAVEN_OPTS:-} " in
      *"-Djavax.net.ssl.trustStore="*) ;;
      *) export MAVEN_OPTS="${MAVEN_OPTS:+$MAVEN_OPTS }-Djavax.net.ssl.trustStore=$custom_cacerts -Djavax.net.ssl.trustStorePassword=changeit" ;;
    esac
  fi

  # 4. Persistir variables en ~/.bashrc para futuras shells en la VM actual
  local bashrc="$HOME/.bashrc"
  local marker="# >>> bidmaster-env >>>"
  if [ -f "$bashrc" ] && ! grep -qF "$marker" "$bashrc"; then
    cat >> "$bashrc" <<EOF

$marker
[ -d "$jdk_dir/bin" ] && export JAVA_HOME="$jdk_dir" && export PATH="$jdk_dir/bin:\$PATH"
[ -d "$bin_dir" ] && export PATH="$bin_dir:\$PATH"
[ -f "$custom_cacerts" ] && case " \${MAVEN_OPTS:-} " in *"-Djavax.net.ssl.trustStore="*) ;; *) export MAVEN_OPTS="\${MAVEN_OPTS:+\$MAVEN_OPTS }-Djavax.net.ssl.trustStore=$custom_cacerts -Djavax.net.ssl.trustStorePassword=changeit" ;; esac
# <<< bidmaster-env <<<
EOF
  fi

  echo "Entorno listo:"
  echo "  JAVA_HOME=${JAVA_HOME:-$active_java_home}"
  java -version 2>&1 | head -n 1 | sed 's/^/  /'
  echo "  openspec=$(command -v openspec 2>/dev/null || echo 'no disponible')"
}

_bidmaster_setup_env
unset -f _bidmaster_setup_env
