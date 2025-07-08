#!/bin/bash

# FXGL Upstream Sync Script
# Automatically syncs the upstream branch with the original FXGL repository
# Based on CLAUDE.md instructions for branch management

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[SYNC]${NC} $1"
}

print_header "🔄 Starting FXGL upstream sync process..."

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    print_error "Not in FXGL root directory (no pom.xml found)"
    exit 1
fi

# Store current branch
CURRENT_BRANCH=$(git branch --show-current)
print_status "Current branch: $CURRENT_BRANCH"

# Check if original remote exists
if ! git remote | grep -q "^original$"; then
    print_warning "Original remote not found. Adding it now..."
    git remote add original https://github.com/AlmasB/FXGL.git
    print_status "Added original FXGL repository as 'original' remote"
fi

# Fetch latest changes from original repository
print_status "Fetching latest changes from original FXGL repository..."
if ! git fetch original; then
    print_error "Failed to fetch from original repository"
    exit 1
fi

# Check if upstream branch exists locally
if git branch | grep -q "upstream"; then
    print_status "Local upstream branch exists, switching to it..."
    git checkout upstream
else
    print_status "Creating local upstream branch from original/dev..."
    git checkout -b upstream original/dev
fi

# Merge or reset to match original/dev exactly
print_status "Syncing upstream branch with original/dev..."

# Check if there are local commits on upstream that don't exist in original/dev
LOCAL_COMMITS=$(git rev-list --count original/dev..HEAD 2>/dev/null || echo "0")

if [ "$LOCAL_COMMITS" -gt 0 ]; then
    print_warning "Found $LOCAL_COMMITS local commits on upstream branch"
    print_warning "These will be preserved during sync"
    
    # Merge original/dev into upstream
    if ! git merge original/dev --no-edit; then
        print_error "Merge conflict detected!"
        print_error "Please resolve conflicts manually and run:"
        print_error "  git add ."
        print_error "  git commit"
        print_error "  git push origin upstream"
        exit 1
    fi
    
    print_status "Successfully merged original/dev into upstream branch"
else
    # No local commits, safe to reset
    print_status "No local commits found, fast-forwarding to original/dev..."
    git reset --hard original/dev
    print_status "Successfully fast-forwarded upstream to match original/dev"
fi

# Push updated upstream branch to origin
print_status "Pushing updated upstream branch to origin..."
if git push origin upstream; then
    print_status "✅ Successfully pushed upstream branch to origin"
else
    print_warning "Failed to push upstream branch (may need force push)"
    print_status "To force push (use with caution): git push --force-with-lease origin upstream"
fi

# Return to original branch
if [ "$CURRENT_BRANCH" != "upstream" ]; then
    print_status "Returning to original branch: $CURRENT_BRANCH"
    git checkout "$CURRENT_BRANCH"
fi

# Show sync summary
print_header "📊 Sync Summary"
echo
print_status "Upstream branch status:"
git log --oneline -5 upstream

echo
print_status "Branches:"
print_status "  • upstream: Synced with original FXGL (Java 23)"
print_status "  • java17-compatible: Your Java 17 development branch"
print_status "  • Current: $CURRENT_BRANCH"

echo
print_header "🎉 Upstream sync completed successfully!"
print_status "The upstream branch now contains the latest changes from the original FXGL repository."

if [ "$CURRENT_BRANCH" = "java17-compatible" ]; then
    echo
    print_warning "Next steps (optional):"
    print_warning "  1. Review changes: git log upstream --oneline"
    print_warning "  2. Cherry-pick specific commits: git cherry-pick <commit-hash>"
    print_warning "  3. Or merge upstream changes: git merge upstream"
fi