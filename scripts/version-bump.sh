#!/bin/bash

# HIIT Timer App - Version Management Script
# Usage: ./scripts/version-bump.sh [major|minor|patch] [--dry-run]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BUILD_GRADLE="app/build.gradle.kts"
VERSION_PATTERN='versionName = "([0-9]+\.[0-9]+\.[0-9]+)"'

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

show_usage() {
    echo "Usage: $0 [major|minor|patch] [--dry-run]"
    echo ""
    echo "Options:"
    echo "  major     Increment major version (1.0.0 -> 2.0.0)"
    echo "  minor     Increment minor version (1.0.0 -> 1.1.0)"
    echo "  patch     Increment patch version (1.0.0 -> 1.0.1)"
    echo "  --dry-run Show what would be changed without making changes"
    echo ""
    echo "Examples:"
    echo "  $0 patch              # Bump patch version"
    echo "  $0 minor --dry-run    # Show minor version bump without applying"
}

get_current_version() {
    if [[ -f "$BUILD_GRADLE" ]]; then
        grep -oP "$VERSION_PATTERN" "$BUILD_GRADLE" | grep -oP '[0-9]+\.[0-9]+\.[0-9]+'
    else
        log_error "Build file not found: $BUILD_GRADLE"
        exit 1
    fi
}

increment_version() {
    local version=$1
    local type=$2
    
    IFS='.' read -ra VERSION_PARTS <<< "$version"
    local major=${VERSION_PARTS[0]}
    local minor=${VERSION_PARTS[1]}
    local patch=${VERSION_PARTS[2]}
    
    case $type in
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch)
            patch=$((patch + 1))
            ;;
        *)
            log_error "Invalid version type: $type"
            exit 1
            ;;
    esac
    
    echo "$major.$minor.$patch"
}

update_version_code() {
    local build_gradle=$1
    local current_code
    
    current_code=$(grep -oP 'versionCode = \K[0-9]+' "$build_gradle")
    local new_code=$((current_code + 1))
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_info "Would update versionCode: $current_code -> $new_code"
    else
        sed -i "s/versionCode = $current_code/versionCode = $new_code/" "$build_gradle"
        log_success "Updated versionCode: $current_code -> $new_code"
    fi
}

update_version_name() {
    local build_gradle=$1
    local current_version=$2
    local new_version=$3
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_info "Would update versionName: $current_version -> $new_version"
    else
        sed -i "s/versionName = \"$current_version\"/versionName = \"$new_version\"/" "$build_gradle"
        log_success "Updated versionName: $current_version -> $new_version"
    fi
}

create_git_tag() {
    local version=$1
    local tag="v$version"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_info "Would create git tag: $tag"
        log_info "Would commit changes with message: 'chore: bump version to $version'"
    else
        # Check if there are uncommitted changes
        if ! git diff-index --quiet HEAD --; then
            log_warning "There are uncommitted changes. Committing version bump..."
            git add "$BUILD_GRADLE"
            git commit -m "chore: bump version to $version"
        fi
        
        # Create and push tag
        git tag -a "$tag" -m "Release version $version"
        log_success "Created git tag: $tag"
        
        log_info "To push the tag, run: git push origin $tag"
        log_info "This will trigger the release workflow"
    fi
}

validate_environment() {
    # Check if we're in the right directory
    if [[ ! -f "$BUILD_GRADLE" ]]; then
        log_error "Not in the project root directory or build.gradle.kts not found"
        exit 1
    fi
    
    # Check if git is available
    if ! command -v git &> /dev/null; then
        log_error "Git is not installed or not in PATH"
        exit 1
    fi
    
    # Check if we're in a git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        log_error "Not in a git repository"
        exit 1
    fi
}

# Main script
main() {
    local version_type=""
    local dry_run=false
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            major|minor|patch)
                version_type=$1
                shift
                ;;
            --dry-run)
                dry_run=true
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Validate arguments
    if [[ -z "$version_type" ]]; then
        log_error "Version type is required"
        show_usage
        exit 1
    fi
    
    # Set global dry run flag
    DRY_RUN=$dry_run
    
    # Validate environment
    validate_environment
    
    # Get current version
    local current_version
    current_version=$(get_current_version)
    log_info "Current version: $current_version"
    
    # Calculate new version
    local new_version
    new_version=$(increment_version "$current_version" "$version_type")
    log_info "New version: $new_version"
    
    if [[ "$dry_run" == "true" ]]; then
        log_warning "DRY RUN MODE - No changes will be made"
    fi
    
    # Update version name and code
    update_version_name "$BUILD_GRADLE" "$current_version" "$new_version"
    update_version_code "$BUILD_GRADLE"
    
    # Create git tag
    create_git_tag "$new_version"
    
    if [[ "$dry_run" == "false" ]]; then
        log_success "Version bump completed successfully!"
        log_info "Next steps:"
        log_info "1. Review the changes: git diff"
        log_info "2. Push the tag: git push origin v$new_version"
        log_info "3. This will trigger the release workflow"
    else
        log_info "Dry run completed. Use without --dry-run to apply changes."
    fi
}

# Run main function
main "$@"
