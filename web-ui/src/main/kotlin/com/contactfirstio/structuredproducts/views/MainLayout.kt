package com.contactfirstio.structuredproducts.views

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.Footer
import com.vaadin.flow.component.html.Header
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.Layout
import com.vaadin.flow.theme.lumo.LumoUtility

@Layout
class MainLayout : AppLayout() {

    init {
        addClassName("app-shell")
        primarySection = Section.DRAWER
        addToDrawer(createDrawerHeader(), Scroller(createNavigation()), createDrawerFooter())
        addToNavbar(true, createNavbar())
    }

    private fun createDrawerHeader(): Header =
        Header().apply {
            addClassName("drawer-brand")
            add(
                Span("Structured Products").apply { addClassName("drawer-brand-title") },
                Span("Trading platform").apply { addClassName("drawer-brand-subtitle") },
            )
        }

    private fun createNavigation(): SideNav {
        val nav = SideNav()
        nav.addItem(SideNavItem("Home", HomeView::class.java, VaadinIcon.HOME.create()))
        nav.addItem(SideNavItem("Data Blotter", BlotterView::class.java, VaadinIcon.TABLE.create()))
        nav.addItem(SideNavItem("Create Product Type", CreateProductTypeView::class.java, VaadinIcon.COG.create()))
        nav.addItem(SideNavItem("Create Product", CreateProductView::class.java, VaadinIcon.PLUS.create()))
        nav.addItem(SideNavItem("Order Entry", OrderEntryView::class.java, VaadinIcon.CART.create()))
        return nav
    }

    private fun createDrawerFooter(): Footer =
        Footer().apply {
            addClassName("drawer-footer")
            text = "Vaadin 25 · Spring Boot 4 · Kotlin"
        }

    private fun createNavbar(): HorizontalLayout {
        val toggle = DrawerToggle()
        val title = Span("Structured Products Platform")
        title.addClassName("app-navbar-title")

        val badge = Span("Demo Environment")
        badge.addClassName("app-navbar-badge")

        return HorizontalLayout(toggle, title, badge).apply {
            addClassName("app-navbar")
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
            setWidthFull()
            addClassNames(LumoUtility.Padding.Horizontal.MEDIUM)
        }
    }
}
